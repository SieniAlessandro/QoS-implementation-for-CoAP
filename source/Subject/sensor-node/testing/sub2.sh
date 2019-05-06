cd ../
make TARGET=z1 MOTE=2 login | ts '%M:%S' > testing/log2.txt
cd testing/
