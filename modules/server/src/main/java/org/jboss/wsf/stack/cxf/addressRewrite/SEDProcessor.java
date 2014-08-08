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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SEDProcessor {

   private static enum SEDCommand {
      SUBSTITUTE, TRANSLATE
   }

   private static final int[] EMPTY_OCCURRENCE = new int[0];

   private final SEDCommand command;
   private final String replacement;
   private final int[] occurrences;
   private final Pattern regexp;
   private final SedArguments args;
   private final CharMap charMap;

   private SEDProcessor(String script, SEDCommand command)
   {
      this.command = command;
      this.args = deriveArgs(script);
      if (args.isIgnoreCase())
      {
         this.regexp = Pattern.compile(getRegexp(args), Pattern.CASE_INSENSITIVE);
      }
      else
      {
         this.regexp = Pattern.compile(getRegexp(args));
      }
      this.replacement = getReplacement(args);
      this.occurrences = args.isOccurrenceSet() ? args.getOccurrence() : EMPTY_OCCURRENCE;
      for (int i = 0; i < occurrences.length; i++)
      {
         if (occurrences[i] <= 0)
         {
            throw new IllegalArgumentException("invalid occurrence index " + occurrences[i] + " in sed command");
         }
      }
      Arrays.sort(occurrences);
      if (command == SEDCommand.TRANSLATE)
      {
         this.charMap = new CharMap(args.getString1(), args.getString2());
      }
      else
      {
         this.charMap = null;
      }
   }

   /**
     * Returns the regexp operand from args, either called "regexp" or
     * "string1". If none of the two is set, an empty string is returned.
     * 
     * @param args
     *            the args with operand values
     * @return the regexp argument from "regexp" or "string1" or an empty string
     *         of none of the two operands is set
     */
   private static String getRegexp(SedArguments args)
   {
      if (args.isRegexpSet())
      {
         return args.getRegexp();
      }
      if (args.isString1Set())
      {
         return args.getString1();
      }
      return "";
   }

   /**
    * Returns the replacement operand from args, either called "replacement" or
    * "string2". If none of the two is set, an empty string is returned.
    * 
    * @param args
    *            the args with operand values
    * @return the replacement argument from "replacement" or "string2" or an
    *         empty string of none of the two operands is set
    */
   private static String getReplacement(SedArguments args)
   {
      if (args.isReplacementSet())
      {
         return args.getReplacement();
      }
      if (args.isString2Set())
      {
         return args.getString2();
      }
      return "";
   }

   private SedArguments deriveArgs(String script)
   {
      final int start = findStartTrimWhitespace(script) + 1;
      final int mid = indexOfNextDelimiter(script, start);
      final int end = indexOfNextDelimiter(script, mid);
      if (mid < 0 || end < 0)
      {
         throw new IllegalArgumentException("invalid script for sed command: " + script);
      }
      if (command == SEDCommand.SUBSTITUTE)
      {
         SedArguments args = parseSubstituteFlags(script, end + 1);
         args.setRegexp(script.substring(start + 1, mid));
         args.setReplacement(script.substring(mid + 1, end));
         return args;
      }
      else if (command == SEDCommand.TRANSLATE)
      {
         SedArguments args = new SedArguments();
         args.setScript(script);
         args.setTranslate(true);
         final int scriptEnd = findEndTrimWhitespace(script);
         if (end + 1 < scriptEnd)
         {
            throw new IllegalArgumentException("non-whitespace characters found after " + command + " command in sed script: " + script);
         }
         args.setString1(script.substring(start + 1, mid));
         args.setString2(script.substring(mid + 1, end));
         return args;
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   private static SedArguments parseSubstituteFlags(String script, int start)
   {
      final int end = findWhitespace(script, start);
      if (end < findEndTrimWhitespace(script))
      {
         throw new IllegalArgumentException("extra non-whitespace characters found after substitute command in sed script: " + script);
      }
      SedArguments args = new SedArguments();
      args.setScript(script);
      args.setSubstitute(true);
      if (start < end)
      {
         //g, I flags
         int index;
         for (index = end - 1; index >= start; index--)
         {
            final char flag = script.charAt(index);
            if (flag == 'g')
            {
               args.setGlobal(true);
            }
            else if (flag == 'I')
            {
               args.setIgnoreCase(true);
            }
            else
            {
               break;
            }
         }
         //occurrence index
         if (index >= start)
         {
            final String occurrenceStr = script.substring(start, index + 1);
            final int occurrence;
            try
            {
               occurrence = Integer.parseInt(occurrenceStr);
            }
            catch (NumberFormatException e)
            {
               throw new IllegalArgumentException("invalid substitute flags in sed script: " + script, e);
            }
            if (occurrence <= 0)
            {
               throw new IllegalArgumentException("invalid occurrence index " + occurrence + " in sed script: " + script);
            }
            args.setOccurrence(occurrence);
         }
      }
      return args;
   }

   public String processLine(String line)
   {
      if (command == SEDCommand.SUBSTITUTE)
      {
         final Matcher matcher = regexp.matcher(line);
         if (matcher.find())
         {
            boolean matches = true;
            final StringBuffer changed = new StringBuffer();//cannot use StringBuilder here since matcher does not support it
            if (occurrences.length > 0)
            {
               int current = 1;
               for (int i = 0; i < occurrences.length; i++)
               {
                  final int occurrence = occurrences[i];
                  while (matches && current < occurrence)
                  {
                     matches = matcher.find();
                     current++;
                  }
                  if (matches)
                  {
                     matcher.appendReplacement(changed, replacement);
                  }
                  else
                  {
                     break;
                  }
               }
               if (matches && occurrences.length == 1 && args.isGlobal())
               {
                  matches = matcher.find();
                  while (matches)
                  {
                     matcher.appendReplacement(changed, replacement);
                     matches = matcher.find();
                  }
               }
            }
            else
            {
               while (matches)
               {
                  matcher.appendReplacement(changed, replacement);
                  matches = args.isGlobal() && matcher.find();
               }
            }
            matcher.appendTail(changed);
            return changed.toString();
         }
         else
         {
            return line;
         }
      }
      else if (command == SEDCommand.TRANSLATE)
      {
         char[] changed = null;
         final int len = line.length();
         for (int i = 0; i < len; i++)
         {
            final char src = line.charAt(i);
            final char dst = charMap.map(src);
            if (dst != 0)
            {
               if (changed == null)
               {
                  changed = new char[len];
                  for (int j = 0; j < i; j++)
                  {
                     changed[j] = line.charAt(j);
                  }
               }
               changed[i] = dst;
            }
            else
            {
               if (changed != null)
               {
                  changed[i] = src;
               }
            }
         }
         return changed != null ? String.valueOf(changed) : line;
      }
      else
      {
         return null;
      }
   }

   /**
     * Returns the index of the next delimiter in the given sed script. The
     * character at {@code indexOfPreviousDelimiter} is taken as delimiter. The
     * method handles escaped delimiters and returns -1 if no further delimiter
     * is found.
     * 
     * @param script
     *            the script to analyze
     * @param indexOfPreviousDelimiter
     *            the index of the previous delimiter
     * @return the index of the next delimiter after
     *         {@code indexOfPreviousDelimiter}, or -1 if no further delimiter
     *         exists of if {@code indexOfNextDelimiter < 0}
     */
   private static int indexOfNextDelimiter(String script, int indexOfPreviousDelimiter)
   {
      if (indexOfPreviousDelimiter < 0 || script.length() <= indexOfPreviousDelimiter)
      {
         return -1;
      }
      final char delim = script.charAt(indexOfPreviousDelimiter);
      if (delim == '\\')
      {
         throw new IllegalArgumentException("invalid delimiter '\\' in sed script: " + script);
      }
      int index = indexOfPreviousDelimiter;
      do
      {
         index = script.indexOf(delim, index + 1);
      }
      while (index >= 0 && isEscaped(script, index));
      return index;
   }

   private static boolean isEscaped(String script, int index)
   {
      int backslashCount = 0;
      index--;
      while (index >= 0 && script.charAt(index) == '\\')
      {
         backslashCount++;
         index--;
      }
      // an uneven count of backslashes means that the character at position
      // index is escaped (an even count of backslashes escapes backslashes)
      return backslashCount % 2 == 1;
   }

   /**
    * Finds and returns the start of the given sequence after trimming
    * whitespace characters from the left.
    * 
    * @param s
    *            the character sequence
    * @return the index containing the first non-whitespace character, or the
    *         length of the character sequence if all characters are blank
    */
   private static int findStartTrimWhitespace(CharSequence s)
   {
      final int len = s.length();
      for (int i = 0; i < len; i++)
      {
         if (!Character.isWhitespace(s.charAt(i)))
         {
            return i;
         }
      }
      return len;
   }

   /**
    * Finds and returns the first whitespace character in the given sequence at
    * or after start. Returns the length of the string if no whitespace is
    * found.
    * 
    * @param s
    *            the character sequence
    * @param start
    *            the first index to consider in the char sequence
    * @return the index containing the first whitespace character at or after
    *         start, or the length of the character sequence if all characters
    *         are blank
    */
   private static int findWhitespace(CharSequence s, int start)
   {
      final int len = s.length();
      for (int i = start; i < len; i++)
      {
         if (Character.isWhitespace(s.charAt(i)))
         {
            return i;
         }
      }
      return len;
   }

   /**
    * Finds and returns the end of the given character sequence after trimming
    * white space characters from the right. Whitespace characters are defined
    * by {@link Character#isWhitespace(char)}. .
    * 
    * @param s
    *            the character sequence
    * @return the index after the last non-whitespace character, or zero if all
    *         characters are blank
    */
   private static int findEndTrimWhitespace(CharSequence s)
   {
      for (int i = s.length(); i > 0; i--)
      {
         if (!Character.isWhitespace(s.charAt(i - 1)))
         {
            return i;
         }
      }
      return 0;
   }

   public static SEDProcessor newInstance(String script)
   {
      final int len = script.length();
      final int scriptStart = findStartTrimWhitespace(script);
      if (scriptStart < len)
      {
         final char firstChar = script.charAt(scriptStart);
         if (firstChar == 's')
         {
            return new SEDProcessor(script, SEDCommand.SUBSTITUTE);
         }
         else if (firstChar == 'y')
         {
            return new SEDProcessor(script, SEDCommand.TRANSLATE);
         }
      }
      throw new IllegalArgumentException("invalid script");
   }
}