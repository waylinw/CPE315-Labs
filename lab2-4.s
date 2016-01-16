.data
   prompt : .asciiz "Enter value to format:\n0000 0000 0000 0000 yyyy 000x 0fff 00nn\n"
   invalid : .asciiz "Error: Invalid Input\n"
   masksArray : .word 0x06000000,0xE0000000,0x00080000,0x0000F000
   newLine : .asciiz "\n"

.text
.globl main
main:
   andi $t9, $t9, 0                 #set t9 to 0
   la $s1, masksArray               #put address of mask into s1
   li $v0, 4

   la $a0, prompt

   syscall                          #print prompt to get data
   li $v0, 5
   syscall                          #system call to get input
   move $t1, $v0                    #puts user input into t1
   li $v0, 4
   la $a0, newLine
   syscall

   beq $zero, $t9, validIn          #if there has been a valid input go to valid input
   bne $zero, $t1, validIn          #
   jr $ra                           #

   validIn:
      andi $t9, $t9, 0              #clear t9 (holds invalid input)
      beq $zero, $t1, check_input   #if input is zero go to invalid section

      lw $t0, ($s1)                 #loads from address of first bit field into t0
      and $s2, $t1, $t0             #masks n (x06000000)
      addi $s1, $s1, 4              #adds 4 to get to next address of bit field

      lw $t0, ($s1)                 #loads the mask value into t0
      and $s3, $t1, $t0             #masks f (xE0000000)
      addi $s1, $s1, 4              #adds 4 to get to next address of bit field

      lw $t0,($s1)                  #loads maks value into t0
      and $s4, $t1, $t0             #masks x (x00080000)
      addi $s1, $s1, 4              #adds 4 to get to next address of bit field

      lw $t0, ($s1)                 #loads maks value
      and $s5, $t1, $t0             #masks y (x0000F000)

      srl $s2, $s2, 25              #n is shifted right 25 bits
      srl $s3, $s3, 25              #f is shifted right 25 bits
      srl $s4, $s4, 11              #x is shifted right 11 bits

      andi $s6, $s6, 0              #clears s6 to 0

      or $s6, $s6, $s2              #or s6 with n
      or $s6, $s6, $s3              #or s6 with f
      or $s6, $s6, $s4              #or s6 with x
      or $s6, $s6, $s5              #or s6 with y

      li $v0, 1                     #system call to print all bits added together
      move $a0, $s6
      syscall

      li $v0, 4                     #
      la $a0, newLine               #prints a new line
      syscall                       #

      jal END                       #jumps to end to skip invalid

   check_input:
      li $v0, 4
      la $a0, invalid
      syscall                       #prints invalid output message

      slti $t9, $t1, 1              #if statement to $t9 to 1 for error input

   END:
      li $v0, 10
      syscall                       #system call to end program #ends program

.end
