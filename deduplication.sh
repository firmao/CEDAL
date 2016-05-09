for file in *.nt
do
	cat $file | awk '{ print $1 $2 $3 }' | uniq | sort | uniq >> /home/andre/Downloads/LinkLion/correct/$file
done
