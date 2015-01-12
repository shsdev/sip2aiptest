#!/bin/bash
PYTHON_PATH="/usr/bin/python"
FIDO_PATH="/usr/local/fido/fido.py"

AWK=awk
OS=`uname -s`
case "$OS" in
SunOS)
        AWK=gawk
        ;;
esac


OUT=`$PYTHON_PATH $FIDO_PATH "$1" | grep "OK" | $AWK 'BEGIN {FS=",";};{printf $3",";}'` 

length=${#OUT}
printf ${OUT:0:(($length - 1))}
