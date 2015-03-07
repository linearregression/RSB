#!/bin/sh

#
#   R Service Bus
#   
#   Copyright (c) Copyright of Open Analytics NV, 2010-2015
#
#   ===========================================================================
#
#   This program is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
#   @author rsb.development@openanalytics.eu
#

# Check mandatory system properties
if [ -z "$R_HOME" ]; then
  echo "R_HOME not set"
  exit 1
fi

if [ -z "$R_LIBS" ]; then
  echo "R_LIBS not set"
  exit 1
fi

echo "Using R_HOME:          $R_HOME"
echo "Using R_LIBS:          $R_LIBS"

PRGDIR=`dirname "$0"`
RSB_WEBAPP_PATH="$PRGDIR"/../webapps/rsb

# Copy security files from /etc/rsb
SRC_FILE=/etc/rsb/security-beans.xml
DST_FILE="$RSB_WEBAPP_PATH"/WEB-INF/classes/META-INF/spring
if [ -f $SRC_FILE ]
then
    echo "Copying $SRC_FILE to $DST_FILE"
    cp $SRC_FILE $DST_FILE
fi

SRC_FILE=/etc/rsb/web.xml
DST_FILE="$RSB_WEBAPP_PATH"/WEB-INF
if [ -f $SRC_FILE ]
then
    echo "Copying $SRC_FILE to $DST_FILE"
    cp $SRC_FILE $DST_FILE
fi

# Start Tomcat
exec "$PRGDIR"/startup.sh "$@"
