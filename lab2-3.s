.data 
	mask: .word 0xF0000000
	split: .word 0x0000000A
	count: .word 28
	input: .word 12345678
   test:  .word 99999999
	stge: .space 10
   word_1: .word 0x12345678, 0x12345678 #first 64 bit int, ahi,alo
   word_2: .word 0x12345678, 0x12345678 #second 64bit int, bhi,blo

.text

.globl main

main:

   subu $sp, $sp, 12    #make 8 bytes available for stack
   sw $ra, 4($sp)      #store return address
   sw $fp, 0($sp)      #store frame pointer

   la $s0, word_1       #load 1st 64 bit array
   lw $a0, 0($s0)       #load ahi
   lw $a1, 4($s0)       #load alo

   la $s0, word_2       #load 2nd 64 bit array
   lw $a2, 0($s0)       #load bhi
   lw $a3, 4($s0)       #load blo

   jal add_64           #calls function to add

   move $a0, $v0        #print out sum hi
   jal bintohex

   move $a0, $v1        #print out sum low
   jal bintohex

   li $v0, 10
   syscall

add_64:
   addu $v1, $a1, $a2   #add alo, blo
   addu $v0, $a0, $a2   #add ahi, bhi
   srl $t0, $a1, 1      #shift right by 1
   srl $t1, $a3, 1      #shift right by 1
   addu $t2, $t0, $t1   #add alo, blo into t2 to check for carry

   andi $t3, $a1, 1
   andi $t3, $t3, 1
   addu $t2, $t2, $t3
   srl $t2, $t2, 31 #gets highest order bit in $t2
   beq $t2, $zero, no_carry #increment hisum only if carry
   addi $v0, $v0, 1
   no_carry:
   jr $ra

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
