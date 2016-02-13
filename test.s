.text
main:
loop1:	
	srl $t0, $t0, 4             #shift right logical
	andi $t1, $t0, 0x0001       #add 1
	bne $t1, $zero, loop1
	and $t0, $t0, $t0
.end
