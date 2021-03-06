.data 
	mask: .word 0xF0000000
	split: .word 0x0000000A
	count: .word 28
	input: .word 12345678
   test:  .word 99999999
	stge: .space 10
.text

.globl main

main:
   la $s3, input              #load input integer into$a0 from memory
   lw $a0, 0($s3)             #test comment

   jal bintohex

   la $a0, test
   lw $a0, 0($a0)
   jal bintohex

   li $v0, 10
   syscall

bintohex:
      #it's up to you to decide from which register you want to load the integer
      #my example uses $s3

     #move $a0, $s3              #load input integer into $a0 from memory
   la $a1, stge               #load hex string storage address into $a1

   la $s0, mask               #load mask into $s0
   lw $s0, 0($s0)
   la $s1, count              #load count into $s1
   lw $s1, 0($s1)
   la $s2, split              #load split into $s2
   lw $s2, 0($s2)

	loop:
		and $t1, $s0, $a0 		#load the result of $t0 and $a0 into $t1
		srl $t1, $t1, $s1
		addi $s1, $s1, -4       #decrement the count by 4 bits (1 byte)
		srl $s0, $s0, 4
		bge $t1, $s2, letter 	#branch to letter if $t1 contains alphabetical hex value (A-F)
		b number                #branch to number if $t1 contains numberical hex value (0-9)

	letter:
		addi $t1, $t1, 55 		#obtain ascii value and store into stge
		sb $t1, 0($a1)
		addi $a1, $a1, 1        #move up stge array pointer by 4
		blt $s1, $0, finish		#if counter is 0 branch to fin
		b loop

	number:
		addi $t1, $t1, 48 		#obtain ascii value and store into stge
		sb $t1, 0($a1)
		addi $a1, $a1, 1        #move up stge array pointer by 4
		blt $s1, $0, finish		#if counter is 0 branch to fin
		b loop

	finish:
      addi $t1, $t1, 1        #add null terminator at the end of the storage area
		li $t1, 0
		sb $t1, 0($a1)
		li $v0, 4               #load up syscalll for printing
      la $a0, stge            #print the value from the storage area
      syscall
jr $ra
.end
