.data 
	mask: .word 0xF0000000
	nums: .word 0x0000000A
	count: .word 28
	input: .word 12345678
	stge: .space 10
.text

.globl main

main:
	la $a0, input				#load input integer into$a0 from memory
	lw $a0, 0($a0)
	la $a1, stge				#load hex string storage address into $a1

	la $s0, mask				#load mask into $s0
	lw $s0, 0($s0)
	la $s1, count 				#load count into $s1
	lw $s1, 0($s1)		
	la $s2, nums				#load nums into $s2
	lw $s2, 0($s2)	

	loop:
		and $t1, $s0, $a0 		#load the result of $t0 and $a0 into $t1
		srl $t1, $t1, $s1
		addi $s1, $s1, -4       #decrement the count by 4 bits (1 byte)
		srl $s0, $s0, 4
		bge $t1, $s2, letter 	#branch to hexup if $t1 contains alphabetical hex value (A-F)
		b number                #branch to hexdown if $t1 contains numberical hex value (0-9)

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
      addi $t1, $t1, 1
		li $t1, 0
		sb $t1, 0($a1)
		li $v0, 4               #load up syscalll for printing
      la $a0, stge
      syscall

		li $v0, 10
		syscall
.end
