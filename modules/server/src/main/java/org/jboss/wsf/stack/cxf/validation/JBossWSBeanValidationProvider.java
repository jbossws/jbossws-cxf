/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.stack.cxf.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ExecutableValidator;
import javax.validation.executable.ValidateOnExecution;

import org.apache.cxf.validation.BeanValidationProvider;
import org.apache.cxf.validation.ResponseConstraintViolationException;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;

/**
 * JBossWS version of Apache CXF BeanValidationProvider that
 * processes @ValidateOnExecution annotation before actually
 * triggering validation.
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public final class JBossWSBeanValidationProvider extends BeanValidationProvider
{
   private static final ExecutableType[] defaultValidatedExecutableTypes =
   {ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS};

   /**
    * Used for resolving type parameters. Thread-safe.
    */
   private TypeResolver typeResolver = new TypeResolver();

   private volatile Validator contextValidator = null;

   private ValidatorFactory factory = null;
   
   private final Map<Method, Boolean> methodsMap = new HashMap<>();

   public JBossWSBeanValidationProvider(List<Class<?>> resources)
   {
      //TODO: Add a getValidatorFactory in cxf to avoid creating factory twice
      factory = Validation.buildDefaultValidatorFactory();
      
      for (Class<?> c : resources) {
         for (Method m : c.getMethods()) {
            if (!m.getDeclaringClass().equals(Object.class)) {
               methodsMap.put(m, isMethodValidatableInternal(m));
            }
         }
      }
   }

   public JBossWSBeanValidationProvider(ValidatorFactory factory)
   {
      this.factory = factory;
   }


   public <T> void validateParameters(final T instance, final Method method, final Object[] arguments)
   {
      if (isMethodValidatable(method)) {
         final ExecutableValidator methodValidator = getExecutableValidator();
         final Set<ConstraintViolation<T>> violations = methodValidator.validateParameters(instance, method, arguments);

         if (!violations.isEmpty())
         {
            throw new ConstraintViolationException(violations);
         }
      }
   }

   public <T> void validateReturnValue(final T instance, final Method method, final Object returnValue)
   {
      if (isMethodValidatable(method)) {
         final ExecutableValidator methodValidator = getExecutableValidator();
         final Set<ConstraintViolation< T > > violations = methodValidator.validateReturnValue(instance, 
             method, returnValue);
         
         if (!violations.isEmpty()) {
             throw new ResponseConstraintViolationException(violations);
         }   
      }
   }

   protected boolean isMethodValidatable(Method m)
   {
      Boolean res = methodsMap.get(m);
      if (res == null) {
         res = isMethodValidatableInternal(m);
      }
      return res;
   }
   
   private boolean isMethodValidatableInternal(Method m)
   {
      ExecutableType[] types = null;
      List<ExecutableType[]> typesList = getExecutableTypesOnMethodInHierarchy(m);
      if (typesList.size() > 1)
      {
         throw new ValidationException("Messages.MESSAGES.validateOnExceptionOnMultipleMethod()"); //TODO i18n
      }
      if (typesList.size() == 1)
      {
         types = typesList.get(0);
      }
      else
      {
         ValidateOnExecution voe = m.getDeclaringClass().getAnnotation(ValidateOnExecution.class);
         if (voe == null)
         {
            types = defaultValidatedExecutableTypes;
         }
         else
         {
            if (voe.type().length > 0)
            {
               types = voe.type();
            }
            else
            {
               types = defaultValidatedExecutableTypes;
            }
         }
      }
      
      boolean isGetterMethod = isGetter(m);
      for (int i = 0; i < types.length; i++)
      {
         switch (types[i])
         {
            case IMPLICIT:
            case ALL:
               return true;
               
            case NONE:
               continue;
               
            case NON_GETTER_METHODS:
               if (!isGetterMethod)
               {
                  return true;
               }
               continue;
               
            case GETTER_METHODS:
               if (isGetterMethod)
               {
                  return true;
               }
               continue;
               
            default: 
               continue;
         }
      }
      return false;
   }
   
   protected List<ExecutableType[]> getExecutableTypesOnMethodInHierarchy(Method method)
   {
      Class<?> clazz = method.getDeclaringClass();
      List<ExecutableType[]> typesList = new ArrayList<ExecutableType[]>();
      
      while (clazz != null)
      {
         // We start by examining the method itself.
         Method superMethod = getSuperMethod(method, clazz);
         if (superMethod != null)
         {
            ExecutableType[] types = getExecutableTypesOnMethod(superMethod);
            if (types != null)
            {
               typesList.add(types);
            }
         }

         typesList.addAll(getExecutableTypesOnMethodInInterfaces(clazz, method));
         clazz = clazz.getSuperclass();
      }
      return typesList;
   }
   
   protected List<ExecutableType[]> getExecutableTypesOnMethodInInterfaces(Class<?> clazz, Method method)
   {
    List<ExecutableType[]> typesList = new ArrayList<ExecutableType[]>();
    Class<?>[] interfaces = clazz.getInterfaces();
    for (int i = 0; i < interfaces.length; i++)
    {
       Method interfaceMethod = getSuperMethod(method, interfaces[i]);
       if (interfaceMethod != null)
       {
          ExecutableType[] types = getExecutableTypesOnMethod(interfaceMethod);
          if (types != null)
          {
             typesList.add(types);
          }
       }
       List<ExecutableType[]> superList = getExecutableTypesOnMethodInInterfaces(interfaces[i], method);
       if (superList.size() > 0)
       {
          typesList.addAll(superList);
       }
    }
    return typesList;
   }

   static protected ExecutableType[] getExecutableTypesOnMethod(Method method)
   {
    ValidateOnExecution voe = method.getAnnotation(ValidateOnExecution.class);
    if (voe == null || voe.type().length == 0)
    {
        return null;
    }
    ExecutableType[] types = voe.type();
    if (types == null || types.length == 0)
    {
        return null;
    }
    return types;
   }
   
   static protected boolean isGetter(Method m)
   {
      String name = m.getName();
      Class<?> returnType = m.getReturnType();
      if (returnType.equals(Void.class))
      {
         return false;
      }
      if (m.getParameterTypes().length > 0)
      {
         return false;
      }
      if (name.startsWith("get"))
      {
         return true;
      }
      if (name.startsWith("is") && returnType.equals(boolean.class))
      {
         return true;
      }
      return false;
   }

   /**
    * Returns a super method, if any, of a method in a class.
    * Here, the "super" relationship is reflexive.  That is, a method
    * is a super method of itself.
    */
   protected Method getSuperMethod(Method method, Class<?> clazz)
   {
      Method[] methods = clazz.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++)
      {
         if (overrides(method, methods[i]))
         {
            return methods[i];
         }
      }
      return null;
   }

   /**
    * Checks, whether {@code subTypeMethod} overrides {@code superTypeMethod}.
    * 
    * N.B. "Override" here is reflexive. I.e., a method overrides itself.
    * 
    * @param subTypeMethod   The sub type method (cannot be {@code null}).
    * @param superTypeMethod The super type method (cannot be {@code null}).
    * 
    * @return Returns {@code true} if {@code subTypeMethod} overrides {@code superTypeMethod}, {@code false} otherwise.
    *         
    * Taken from Hibernate Validator
    */
  protected boolean overrides(Method subTypeMethod, Method superTypeMethod)
  {
     if (subTypeMethod == null || superTypeMethod == null)
     {
        throw new RuntimeException("Messages.MESSAGES.expectTwoNonNullMethods()"); //TODO i18n
     }

     if (!subTypeMethod.getName().equals(superTypeMethod.getName()))
     {
        return false;
     }

     if (subTypeMethod.getParameterTypes().length != superTypeMethod.getParameterTypes().length)
     {
        return false;
     }

     if (!superTypeMethod.getDeclaringClass().isAssignableFrom(subTypeMethod.getDeclaringClass()))
     {
        return false;
     }

     return parametersResolveToSameTypes(subTypeMethod, superTypeMethod);
  }

  /**
   * Taken from Hibernate Validator
   */
  protected boolean parametersResolveToSameTypes(Method subTypeMethod, Method superTypeMethod)
  {
     if (subTypeMethod.getParameterTypes().length == 0)
     {
        return true;
     }

     ResolvedType resolvedSubType = typeResolver.resolve(subTypeMethod.getDeclaringClass());
     MemberResolver memberResolver = new MemberResolver(typeResolver);
     memberResolver.setMethodFilter(new SimpleMethodFilter(subTypeMethod, superTypeMethod));
     ResolvedTypeWithMembers typeWithMembers = memberResolver.resolve(resolvedSubType, null, null);
     ResolvedMethod[] resolvedMethods = typeWithMembers.getMemberMethods();

     // The ClassMate doc says that overridden methods are flattened to one
     // resolved method. But that is the case only for methods without any
     // generic parameters.
     if (resolvedMethods.length == 1)
     {
        return true;
     }

     // For methods with generic parameters I have to compare the argument
     // types (which are resolved) of the two filtered member methods.
     for (int i = 0; i < resolvedMethods[0].getArgumentCount(); i++)
     {

        if (!resolvedMethods[0].getArgumentType(i).equals(resolvedMethods[1].getArgumentType(i)))
        {
           return false;
        }
     }

     return true;
  }
  
  /**
   * A filter implementation filtering methods matching given methods.
   * 
   * @author Gunnar Morling
   * 
   * Taken from Hibernate Validator
   */
  static protected class SimpleMethodFilter implements Filter<RawMethod>
  {
     private final Method method1;
     private final Method method2;

     private SimpleMethodFilter(Method method1, Method method2)
     {
        this.method1 = method1;
        this.method2 = method2;
     }

     @Override
     public boolean include(RawMethod element)
     {
        return element.getRawMember().equals(method1) || element.getRawMember().equals(method2);
     }
  }
  
   public <T> void validateBean(final T bean)
   {
      final Set<ConstraintViolation<T>> violations = doValidateBean(bean);
      if (!violations.isEmpty())
      {
         throw new ConstraintViolationException(violations);
      }
   }
   
   private <T> Set<ConstraintViolation<T>> doValidateBean(final T bean)
   {
      if (contextValidator != null)
      {
         return contextValidator.validate(bean);
      }
      return getValidator().validate(bean);
   }

   private ExecutableValidator getExecutableValidator()
   {
      if (contextValidator != null)
      {
         return contextValidator.forExecutables();
      }
      return getValidator().forExecutables();

   }
   
   
   private synchronized Validator getValidator()
   {
      if (contextValidator != null)
      {
         return contextValidator;
      }
      try
      {
         //get jndi vlidatorFactory to validate cdi beans
         Context context = new InitialContext();
         contextValidator = ValidatorFactory.class.cast(context.lookup("java:comp/ValidatorFactory")).getValidator();
      }
      catch (NamingException e)
      {
         //TODO: i18n log
      }
      if (contextValidator == null) {
         contextValidator = factory.getValidator();
      }
     
      return contextValidator;
   }
   

}