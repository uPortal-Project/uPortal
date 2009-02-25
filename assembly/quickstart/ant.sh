#! /bin/bash
#
# Copyright (c) 2000-2009, Jasig, Inc.
# See license distributed with this file and available online at
# https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
#
export ANT_HOME=`dirname "$0"`/@ant.name@
`dirname "$0"`/@ant.name@/bin/ant $@
