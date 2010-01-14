@REM
@REM Licensed to Jasig under one or more contributor license
@REM agreements. See the NOTICE file distributed with this work
@REM for additional information regarding copyright ownership.
@REM Jasig licenses this file to you under the Apache License,
@REM Version 2.0 (the "License"); you may not use this file
@REM except in compliance with the License. You may obtain a
@REM copy of the License at:
@REM
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on
@REM an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM

@echo off
set ANT_HOME=%~dp0\@ant.name@
%~dp0\@ant.name@\bin\ant.bat %*
