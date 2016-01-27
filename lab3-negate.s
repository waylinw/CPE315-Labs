.data
   num1: .word 0xc0a00000 #5
   signMask: .word 0x80000000 #sign mask
   mask: .word 0xF0000000
	split: .word 0x0000000A
   multiplyResultMask: .word 0x80000000
	count: .word 28
	stge: .space 10
   .text

.globl main

main:
   lw $a0, num1
   addi $sp, $sp, -8 #add space to stack pointer
   sw $a0, 4($sp) #push arg onto stack
   sw $ra, 0($sp) #push ra onto stack
   jal float_negate #calls function

   lw $a0, 4($sp) #load value from stack for printing
   lw $ra, 0($sp) #load ra so we can exit code
   addi $sp, $sp, 8 #adjust stack pointer

   jal bintohex #print out the result

   li $v0, 10
   syscall



#registers used:
#t0 for number
#t1 for 31bit mask
float_negate:
   lw $t0, 4($sp) #load the argument from stack
   lw $t1, signMask #load the 31st bit mask
   xor $t0, $t0, $t1 #xor to save the result back into t0
   sw $t0, 4($sp) #push t0 onto stack to return
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
