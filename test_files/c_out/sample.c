#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include <stdbool.h>
#define MAXCHAR 512

char* integer_to_str(int i){
	int length= snprintf(NULL,0,"%d",i);
	char* result=malloc(length+1);
	snprintf(result,length+1,"%d",i);
	return result;
}
char* real_to_str(float i){
	int length= snprintf(NULL,0,"%f",i);
	char* result=malloc(length+1);
	snprintf(result,length+1,"%f",i);
	return result;
}
char* char_to_str(char i){
	int length= snprintf(NULL,0,"%c",i);
	char* result=malloc(length+1);
	snprintf(result,length+1,"%c",i);
	return result;
}
char* bool_to_str(bool i){
	int length= snprintf(NULL,0,"%d",i);
	char* result=malloc(length+1);
	snprintf(result,length+1,"%d",i);
	return result;
}
char* str_concat(char* str1, char* str2){
	char* result=malloc(sizeof(char)*MAXCHAR);
	result=strcat(result,str1);
	result=strcat(result,str2);
	return result;
}

char* read_str(){
	char* str=malloc(sizeof(char)*MAXCHAR);
	scanf("%s",str);
	return str;
}

int str_to_bool(char* expr){
	int i=0;
	if ( (strcmp(expr, "true")==0) || (strcmp(expr, "1"))==0 )
		i=1;
	if ( (strcmp(expr, "false")==0) || (strcmp(expr, "0"))==0 )
		i=0;
	return i;
}

typedef struct { 
	char* result0;
} result_stampa;

char* stampa(char* messaggio);

void somma(float* result,char* size,float b,int d,int a);

void sommac(float* result,char** size,float b,int d,int a);

int c = 1;


void main(){
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"nok", MAXCHAR);
float risultato = 0.0;
char* ans =  malloc(sizeof(char) * MAXCHAR);
strncpy(ans,"no", MAXCHAR);
char* ans1 = malloc(sizeof(char) * MAXCHAR);
char* taglia = malloc(sizeof(char) * MAXCHAR);
int x = 3;
float b = 2.2;
int a = 1;
sommac(&risultato ,&taglia ,b ,x ,a);
char* r_1 = stampa(str_concat(str_concat(str_concat(str_concat(str_concat(str_concat(str_concat("la somma di ", integer_to_str(a)), " e "), real_to_str(b)), " incrementata di "), integer_to_str(c)), " è "), taglia));
valore = r_1;
char* r_2 = stampa(str_concat("ed è pari a ", real_to_str(risultato)));
valore = r_2;
printf("vuoi continuare? (si/no) - inserisci due volte la risposta");
scanf( "%s", ans);
scanf( "%s", ans1);
while (strcmp(ans, "si")==0) { 
printf("inserisci un intero:");
scanf( "%d", &a);
printf("inserisci un reale:");
scanf( "%f", &b);
sommac(&risultato ,&taglia ,b ,c ,a);
char* r_3 = stampa(str_concat(str_concat(str_concat(str_concat(str_concat(str_concat(str_concat("la somma di ", integer_to_str(a)), " e "), real_to_str(b)), " incrementata di "), integer_to_str(c)), " è "), taglia));
valore = r_3;
char* r_4 = stampa(str_concat(integer_to_str(a), ans));
valore = r_4;
printf("vuoi continuare? (si/no):\t");
scanf( "%s", ans);

}
printf(" \n" );
printf(" ciao " );
}


void somma(float* result,char* size,float b,int d,int a){
*result = a + b + c + d;
if( *result > 100) {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"grande", MAXCHAR);
size =  malloc(sizeof(char) * MAXCHAR);
strncpy(size,valore, MAXCHAR);
}
else if (*result > 50) {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"media", MAXCHAR);
size =  malloc(sizeof(char) * MAXCHAR);
strncpy(size,valore, MAXCHAR);
}
else {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"piccola", MAXCHAR);
size =  malloc(sizeof(char) * MAXCHAR);
strncpy(size,valore, MAXCHAR);
}
stampa(size);
}


void sommac(float* result,char** size,float b,int d,int a){
*result = a + b + c + d;
if( *result > 100) {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"grande", MAXCHAR);
*size =  malloc(sizeof(char) * MAXCHAR);
strncpy(*size,valore, MAXCHAR);
}
else if (*result > 50) {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"media", MAXCHAR);
*size =  malloc(sizeof(char) * MAXCHAR);
strncpy(*size,valore, MAXCHAR);
}
else {
char* valore =  malloc(sizeof(char) * MAXCHAR);
strncpy(valore,"piccola", MAXCHAR);
*size =  malloc(sizeof(char) * MAXCHAR);
strncpy(*size,valore, MAXCHAR);
}
stampa(*size);
}
char*  stampa(char* messaggio) {
int i = 0;
while (i < 4) { 
printf(" \n" );
i = i + 1;

}
printf(" %s ", messaggio );
return "ok";}
