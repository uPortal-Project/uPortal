/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.pags.testers;

/**
 * A tester for matching multiple values of an attribute 
 * against a regular expression.  The match function attempts to find the 
 * next subsequence of the attribute that matches the pattern. 
 * <p>
 * For example, if the pattern is specified as "<strong><code>^02([A-D])*</code></strong>":
 * 
 * <code>
 * <table border='2' width='100%'>
 *  <tr>
 *    <td><strong>Input</strong></td><td><strong>Matches</strong></td>
 *  </tr>
 *  <tr>
 *    <td>02A</td><td>Yes</td>
 *  </tr>
 *  <tr>
 *    <td>02ABCD</td><td>Yes</td>
 *  </tr>
 *  <tr>
 *    <td>A02D</td><td>No</td>
 *  </tr>
 *  <tr>
 *    <td>02</td><td>Yes</td>
 *  </tr>
 *  <tr>
 *    <td>02MisMatch</td><td>Yes</td>
 *  </tr>
 *  <tr>
 *    <td>PatternWillNeverMatch</td><td>No</td>
 *  </tr>
 * </table>
 * </code>
 * 
 * @author Misagh Moayyed
 * @see RegexTester
 */
public class EagerRegexTester extends RegexTester {
    
    public EagerRegexTester(String attribute, String test) {
        super(attribute, test);
    }

    @Override
    public boolean test(String att) {
        return pattern.matcher(att).find();
    }

}
