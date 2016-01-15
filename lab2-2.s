#recursive function that computes the fibonacci seq

.text
main:

   subu $sp, $sp, 4  #make 4 byte stack space
   sw $ra, 4($sp)    #save return address

#read in n
   li $v0, 5      #read Integer
   syscall

   move $a0, $v0  #move value into a0 for function call
   jal fib        #Call fibonacci function

   move $a0, $v0  #print out the value
   li $v0, 1
   syscall

   li $v0, 10     #exit program
   syscall


#fibonacci function
fib:
   addi $sp, $sp, -12   #offset stack ptr by 12 bytes
   sw $s0, 8($sp)       #push saved temp 0 onto stack
   sw $s1, 4($sp)       #push saved temp 1 onto stack
   sw $ra, 0($sp)       #push return address onto stack

   move $s0, $a0        #put n into saved temporary
   bne $s0, $zero, not_zero   #go to not zero if s0 is not 0
   li $v0, 0
   j clean_up

not_zero:
   li $t0, 1
   bne $s0, $t0, not_one   #go to not_one if s0 is not 1
   li $v0, 1
   j clean_up

not_one:
   addi $a0, $s0, -1    #decrement n by 1
   jal fib              #recursively calls fib to find n-1

   move $s1, $v0        #move result from recursion into s1
   addi $a0, $s0, -2    #recursively calls fib to find n-2
   jal fib
   add $v0, $s1, $v0    #add n-1 to n-2 result and save it in v0 for return

clean_up:
   lw $s0, 8($sp) #retrieve s0, s1, ra from stack
   lw $s1, 4($sp)
   lw $ra, 0($sp)
   addi $sp, $sp, 12 #adjust the stack ptr
   jr $ra         #jump register back to the previous caller


.end