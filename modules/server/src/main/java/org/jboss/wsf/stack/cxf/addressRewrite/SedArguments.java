/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.addressRewrite;

final class SedArguments
{
   private boolean isGlobal = false;
   private boolean isIgnoreCase = false;
   private boolean isTranslate = false;
   private boolean isSubstitute = false;
   private String script;
   private boolean scriptIsSet = false;
   private String regexp;
   private boolean regexpIsSet = false;
   private String string1;
   private boolean string1IsSet = false;
   private String replacement;
   private boolean replacementIsSet = false;
   private String string2;
   private boolean string2IsSet = false;
   private int[] occurrence;
   private boolean occurrenceIsSet = false;

   /**
    * Constructor to use if no options are specified.
    */
   public SedArguments()
   {
      
   }

   public String getScript()
   {
      if (scriptIsSet)
      {
         return script;
      }
      throw new IllegalStateException("operand has not been set: " + script);
   }

   public boolean isScriptSet()
   {
      return scriptIsSet;
   }

   public void setScript(String script)
   {
      this.script = script;
      this.scriptIsSet = true;
   }

   public String getRegexp()
   {
      if (regexpIsSet)
      {
         return regexp;
      }
      throw new IllegalStateException("operand has not been set: " + regexp);
   }

   public boolean isRegexpSet()
   {
      return regexpIsSet;
   }

   public void setRegexp(String regexp)
   {
      this.regexp = regexp;
      this.regexpIsSet = true;
   }

   public String getString1()
   {
      if (string1IsSet)
      {
         return string1;
      }
      throw new IllegalStateException("operand has not been set: " + string1);
   }

   public boolean isString1Set()
   {
      return string1IsSet;
   }

   public void setString1(String string1)
   {
      this.string1 = string1;
      this.string1IsSet = true;
   }

   public String getReplacement()
   {
      if (replacementIsSet)
      {
         return replacement;
      }
      throw new IllegalStateException("operand has not been set: " + replacement);
   }

   public boolean isReplacementSet()
   {
      return replacementIsSet;
   }

   public void setReplacement(String replacement)
   {
      this.replacement = replacement;
      this.replacementIsSet = true;
   }

   public String getString2()
   {
      if (string2IsSet)
      {
         return string2;
      }
      throw new IllegalStateException("operand has not been set: " + string2);
   }

   public boolean isString2Set()
   {
      return string2IsSet;
   }

   public void setString2(String string2)
   {
      this.string2 = string2;
      this.string2IsSet = true;
   }

   public int[] getOccurrence()
   {
      if (occurrenceIsSet)
      {
         return occurrence;
      }
      throw new IllegalStateException("operand has not been set: " + occurrence);
   }

   public boolean isOccurrenceSet()
   {
      return occurrenceIsSet;
   }

   public void setOccurrence(int... occurrence)
   {
      this.occurrence = occurrence;
      this.occurrenceIsSet = true;
   }

   public boolean isGlobal()
   {
      return isGlobal;
   }

   public boolean isIgnoreCase()
   {
      return isIgnoreCase;
   }

   public boolean isSubstitute()
   {
      return isSubstitute;
   }

   public boolean isTranslate()
   {
      return isTranslate;
   }

   public void setGlobal(boolean isGlobal)
   {
      this.isGlobal = isGlobal;
   }

   public void setIgnoreCase(boolean isIgnoreCase)
   {
      this.isIgnoreCase = isIgnoreCase;
   }

   public void setTranslate(boolean isTranslate)
   {
      this.isTranslate = isTranslate;
   }

   public void setSubstitute(boolean isSubstitute)
   {
      this.isSubstitute = isSubstitute;
   }

}
