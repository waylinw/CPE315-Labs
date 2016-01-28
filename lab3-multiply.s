.data
   num1: .word 0xc0000000 #-2
   num2: .word 0x40a00000 #5
   expMask: .word 0x7F800000 #exp mask
   matissaMask: .word 0x7FFFFF #matissaMask
   matissaHidden: .word 0x40000000 #matissa hidden 1
   matissaResultMask: .word 0x3FFFFF80 #mask for the result matissa
   mask: .word 0xF0000000
	split: .word 0x0000000A
   multiplyResultMask: .word 0x80000000
	count: .word 28
	stge: .space 10
   .text

.globl main

main:
   lw $a0, num1
   lw $a1, num2
   jal float_multiply

   move $a0, $v0        #print out sum 
   jal bintohex

   li $v0, 10
   syscall



#registers used:
#a0   num1
#a1   num2
#t0   exponent mask, matissa mask
#t1   matissa sum
#t3   shifted sum
#t8   check if we need to negate result
#s0   sign1
#s1   exp1
#s2   matissa1
#s3   sign2
#s4   exp2
#s5   matissa2
#s6   signResult

float_multiply:
   #sign
   srl $s0, $a0, 31 #get sign of num1
   srl $s3, $a1, 31 #get sign of num2

   #exp
   lw $t0, expMask #load exp mask into t0

   and $s1, $a0, $t0 # get the exp for num 1
   srl $s1, $s1, 23  # shift right by 23 bits to get 2's comp
   addi $s1, $s1, -127  # adjust for 127 bias
   and $s4, $a1, $t0 # get the exp for num 2
   srl $s4, $s4, 23 # shift right by 23 bits to get 2's comp
   addi $s4, $s4, -127 # adjust for 127 bias

   #matissa
   lw $t0, matissaMask # load mask to get bit 0-22
   and $s2, $a0, $t0 # get matissa for num 1
   and $s5, $a1, $t0 # get matissa for num 2
   sll $s2, $s2, 7 #shift left by 7 bits to make 32 bit num
   sll $s5, $s5, 7 #shift left by 7 bits to make 32 bit num
   lw $t0, matissaHidden # load matissa hidden 1
   or $s2, $t0, $s2  #add the hidden 1
   or $s5, $t0, $s5  #add the hidden 1

mult_num:
   multu $s2, $s5  #multiply s2, s5
   mfhi $t1       #load the high bits into t1
   sll $t1, $t1, 1  #shift left by 1 bit to adjust for scale
   addi  $s1, $s1, 1 #adjust for the scale

normalize_loop: # shift left till 30th bit is 1
   lw $t0, matissaHidden
   and $t3, $t0, $t1
   beq $t3, $t0, repack
   sll $t1, $t1, 1
   addi $s1, $s1, -1
   j normalize_loop

repack:
   lw $t0, matissaResultMask
   and $t1, $t0, $t1 # get the matissa for the result
   srl $t1, $t1, 7 # shift the result matisa by 7 bits
   sll $s6, $s6, 31 # shift the sign to MSB
   xor $s6, $s0, $s3 #srl $s6, $t1, 31 # getting the sign of the result
   add $s1, $s1, $s4 # add the powers up together
   add $s1, $s1, 127 # add the bias
   sll $s1, $s1, 23 # shift the bias 23 bits

   and $v0, $v0, $zero
   or $v0, $v0, $s6 # add the sign to the result
   or $v0, $v0, $s1 # add the exp to the result
   or $v0, $v0, $t1 # add the matissa to the result

jr $ra

negate:
   lw $t0, 4($sp) # load the number from the stack
   and $t1, $t1, $zero  # zero out a register
   addi $t1, $t1, -1 # get -1
   xor $t0, $t0, $t1 # exclusive or to flip bits
   addi $t0, $t0, 1  # add 1 to t0
   sw $t0, 4($sp)    # save word back onto stack
jr $ra            # return

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
