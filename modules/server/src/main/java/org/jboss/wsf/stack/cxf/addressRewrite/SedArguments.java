/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
