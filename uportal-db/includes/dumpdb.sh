#!/bin/bash

pg_dump --username uportal uportal > /docker-entrypoint-initdb.d/50-uportal.sql
