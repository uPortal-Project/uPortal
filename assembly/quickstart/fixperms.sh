#! /bin/bash
#
# Copyright (c) 2000-2009, Jasig, Inc.
# See license distributed with this file and available online at
# https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
#
find . -name '*.sh' -exec chmod 755 {} \;
chmod 755 ./@ant.name@/bin/*
chmod 755 ./@maven.name@/bin/*
chmod 755 ./@tomcat.name@/bin/*
