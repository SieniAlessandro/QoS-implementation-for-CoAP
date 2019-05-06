cd ../
make TARGET=z1 MOTE=1 login | ts '%M:%S' > testing/log1.txt
cd testing/
