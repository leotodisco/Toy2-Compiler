#!/bin/bash
set -- "my"
#changes $1
mkdir $1.report
if [ $? -ne 0 ]; then
	echo "Cancellare la directory $1.report"
	exit 1
fi

RESULT=0

OUTPUTDIR="test_files/c_out"
mkdir -p $OUTPUTDIR

for TESTDIR in tests/*
do
	TESTNAME=$(basename -- "$TESTDIR")
	TESTFILE=$TESTDIR/$TESTNAME.txt
	echo "" >> $1.report.txt
	echo "" >> $1.report.txt
	echo Test name: $TESTDIR >> $1.report.txt
	echo mvn --batch-mode -q exec:java -Dexec.args="$TESTFILE" >> $1.report.txt
	mvn --batch-mode -q exec:java -Dexec.args="$TESTFILE" >> $1.report.txt 2>&1

	if [ -s "$OUTPUTDIR/$TESTNAME.c" ]; then
		EXEFILE=$OUTPUTDIR/$TESTNAME.out
		echo "" >> $1.report.txt
		echo gcc $OUTPUTDIR/$TESTNAME.c -o $EXEFILE -lm -w >> $1.report.txt
		gcc $OUTPUTDIR/$TESTNAME.c -o $EXEFILE -lm -w >> $1.report.txt 2>&1

		for TESTIN in "$TESTDIR/$TESTNAME"_in*
		do
			TESTINNAME=$(basename -- "$TESTIN")
			TESTOUT=$OUTPUTDIR/"${TESTINNAME/_in/_out}"
			if [ -s "$EXEFILE" ]; then
				$EXEFILE < $TESTIN &> $TESTOUT
			else
				RESULT=1
			fi
			echo "" >> $1.report.txt
			echo diff -w "${TESTIN/_in/_out}" $TESTOUT >> $1.report.txt
			diff -w "${TESTIN/_in/_out}" $TESTOUT >> $1.report.txt
		done
	else 
		echo "$OUTPUTDIR/$TESTNAME.c non esiste" >> $1.report.txt
		if [[ $TESTNAME != *invalid* ]]; then
			RESULT=1
		fi
	fi
done

mkdir $1.report/$(dirname "$OUTPUTDIR")
mv "$OUTPUTDIR" $1.report/$(dirname "$OUTPUTDIR")
mv $1.report.txt $1.report
echo 'WARNING: An illegal reflective access operation has occurred' > $1.report/skip.txt
echo 'WARNING: Illegal reflective access by com.google.inject.internal.cglib.core.$ReflectUtils$1 (file:/usr/share/maven/lib/guice.jar) to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)' >> $1.report/skip.txt
echo 'WARNING: Please consider reporting this to the maintainers of com.google.inject.internal.cglib.core.$ReflectUtils$1' >> $1.report/skip.txt >> $1.report/skip.txt
echo 'WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations' >> $1.report/skip.txt
echo 'WARNING: All illegal access operations will be denied in a future release' >> $1.report/skip.txt
grep -v -x -F -f $1.report/skip.txt $1.report/$1.report.txt
exit $RESULT
