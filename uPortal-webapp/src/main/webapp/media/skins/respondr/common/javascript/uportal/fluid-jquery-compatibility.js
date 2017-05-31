/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
"use strict";

/*
 * The role of this library is to support the continued use of Fluid Infusion 1.4
 * in Respondr for as long as we need it (with any luck, not long).  Boostrap 3
 * requires jQuery 1.9.x or higher;  but Fluid 1.4 (still the most recent)
 * supports only 1.6.x.  uPortal has a lot of interfaces written with Fluid.
 * The short-term strategy will be to deal with this issue as follows:
 * 
 *   - Load jQuery 1.6.x (compatible with Fluid 1.4)
 *   - Load jQueryUI 1.8.x (compatible with Fluid 1.4)
 *   - Load Fluid 1.4
 *   - Remove jQuery 1.6.x from the global namespace (this file)
 *   - Load jQuery 1.10.x (compatible with Bootstrap 3)
 * 
 * This approach means that we will be loading two copies of the jQuery library
 * in the theme alone (before we even consider portlets).  These steps MUST BE 
 * PERFORMED IN THIS ORDER.
 */

jQuery.noConflict(true);
