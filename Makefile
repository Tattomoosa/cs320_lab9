.SUFFIXES:
.SUFFIXES: .mini .dot .pdf

.PHONY: all compiler clean

all:	clean compiler

compiler:
	javac MiniCompiler.java

.mini:
	java MiniCompiler $*

.dot.pdf:
	dot -Tpdf -o $*.pdf $*.dot

clean:
	-rm -f test?.s buggy?.s good?.s test? buggy? good? 
	-rm -f MiniCompiler.class */*.class 

