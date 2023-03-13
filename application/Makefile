OBJS = \
	main.o

all: $(OBJS)
	gcc $< -o eguso -lSDL2

%.o : %.c
	gcc -c $< -o $@
