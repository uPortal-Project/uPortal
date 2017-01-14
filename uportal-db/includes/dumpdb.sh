#!/bin/bash

pg_dump -U uportal --clean --if-exists -C uportal > /docker-entrypoint-initdb.d/50-uportal.sql
