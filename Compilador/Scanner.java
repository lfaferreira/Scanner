/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Lucas Fernando
 */
public class Scanner {

    private Integer LINE = 1; //Marcador de Linhas do codigo para detecção do erro
    private Integer COLUMN = 0; //Marcador de Colunas do codigo para detecção do erro
    private char LOOKAHEAD = ' ';//Buscador do proximo caracter no arquivo lido. 
    private String BUFFER = "";//Buffer responsavel pela criação dos tokens.
    private Deque<String> TOKENS = new ArrayDeque<String>();//Pilha para guardar os tokens criados.
    private BufferedReader arquivoUsuario;
    public Scanner(BufferedReader arquivo) {
        arquivoUsuario = arquivo;
    }

    public void scan() throws FileNotFoundException, IOException { //Metodo principal da Classe Scanner. Starta todo o processo de leitura, classificação de tokens e print dos tokens para o usuario.
        TOKENS = tokenClassification(arquivoUsuario);
        printTokens();
        System.exit(0);
    }

    private Deque<String> tokenClassification(BufferedReader arquivoUsuario) {
        while (LOOKAHEAD != 65535) { //"65535" é o codigo referente ao simbolo de EOF                
            if (validCharacter()) {
                while (Character.isWhitespace(LOOKAHEAD)) { //Verificia se LH é space, quebra de linha ou TAB
                    lookAHead(arquivoUsuario);
                }
                if (Character.isDigit(LOOKAHEAD) || LOOKAHEAD == '.') { //1ª seção de criação dos Tokens. Nesta seção do codigo serão criados os tokens numericos, sejam eles int ou float.
                    tokenNumericoInt(arquivoUsuario);
                }
                if (Character.isLetter(LOOKAHEAD)  || LOOKAHEAD == 95) {//2ª seleção de criação dos Tokens. Nesta seção do codigo serão criados os tokens de Identificadores, sejam eles variaveis ou palavras reservadas.
                    tokenID(arquivoUsuario);
                }
                if (LOOKAHEAD == 39) { //3ª seleção de criação dos Tokens. Nesta seção do codigo serão criados os tokens de Characteres. "39" é o codigo de apostrofo.
                    tokenChar(arquivoUsuario);
                }
                if (LOOKAHEAD == ';' || LOOKAHEAD == ',' || LOOKAHEAD == '}' || LOOKAHEAD == '{' || LOOKAHEAD == '(' || LOOKAHEAD == ')') {//4ª seleção de criação dos Tokens.Nesta seção do codigo serão criados os tokens especiais.
                    tokenSpecial(arquivoUsuario);
                }
                if (LOOKAHEAD == '+' || LOOKAHEAD == '-' || LOOKAHEAD == '*' || LOOKAHEAD == '=' || LOOKAHEAD == '/') {//5ª Seleção de criação do Tokens. Nesta seção do codigo serão criados os tokens de operações artimeticas.
                    tokenAritmetico(arquivoUsuario);
                }
                if (LOOKAHEAD == '<' || LOOKAHEAD == '>' || LOOKAHEAD == '!') { //6ª Seleção de criação do Tokens. Nesta seção do codigo serão criados os tokens de operação relacional.
                    tokenRelacional(arquivoUsuario);
                }
            } else { //Caso o caracter não seja valido na gramatica da linguagem aceita pelo Scanner, ele será invalido.               
                characterInvalid("Character Invalido!");
                System.exit(0);
            }
        }
        return TOKENS;
    }

    private void lookAHead(BufferedReader arquivoUsuario) { //Metodo para leitura do arquivo enviado pelo usuario. A leitura é feita caracter por caracter
        try {
            LOOKAHEAD = (char) arquivoUsuario.read();            
            localization();
        } catch (IOException ex) {
            System.err.println("Erro na leitura binaria do arquivo.");
        }
    }

    private void localization() { //Metodo para identificar em qual local está ocorrendo a leitura do arquivo. Ocorre atraves de cada linha e coluna.
        if (LOOKAHEAD == 13) {
            LINE = LINE + 1;
            COLUMN = 0;
        } else {
            if (LOOKAHEAD == 9) {
                COLUMN = COLUMN + 4;
            } else {
                COLUMN = COLUMN + 1;
            }
        }
    }

    private boolean validCharacter() { //Metodo para validação do caractere lido.
        if (LOOKAHEAD == 34) {//é o codigo de '"'
            return false;
        } else if (LOOKAHEAD == 35) { //é o codigo de '#'
            return false;
        } else if (LOOKAHEAD == 37) {//é o codigo de '%'
            return false;
        } else if (LOOKAHEAD == 38) {//é o codigo de '&'
            return false;
        } else if (LOOKAHEAD == 58) {//é o codigo de ':'
            return false;
        } else if (LOOKAHEAD == 63) {//é o codigo de '?'
            return false;
        } else if (LOOKAHEAD == 64) {//é o codigo de '@'
            return false;
        } else if (LOOKAHEAD == 92) {//é o codigo de '\'
            return false;
        } else if (LOOKAHEAD == 94) {//é o codigo de '^'
            return false;
        } else if (LOOKAHEAD == 96) {//é o codigo de '`'
            return false;
        } else if (LOOKAHEAD == 124) {//é o codigo de '|'
            return false;
        } else if (LOOKAHEAD == 126) {
            return false;
        } else if (LOOKAHEAD == 65533) {//"65533" é o codigo referente ao simbolo de 'ç','Ç','´','ª','º','¨','¬'
            return false;
        }
        return true;
    }

    private void writeToken() { //Metodo para escrita do token no buffer, enquanto ele está sendo classificado.
        BUFFER = BUFFER + LOOKAHEAD;
    }

    private void clearBuffer() {//Metodo para limpeza do buffer.
        BUFFER = "";
    }

    private void tokenNumericoInt(BufferedReader arquivoUsuario) { //Metodo para classificação de Token Numerico do tipo Inteiro.
        while ((LOOKAHEAD >= 48 && LOOKAHEAD <= 57) || LOOKAHEAD == '.') {
            if (LOOKAHEAD == '.') {
                if (BUFFER == "") {
                    BUFFER = 0 + BUFFER;
                }
                tokenNumericoFloat(arquivoUsuario);
                return;
            } else {
                writeToken();
                lookAHead(arquivoUsuario);
            }
        }
        TOKENS.addFirst("60");
        TOKENS.addFirst(BUFFER);
        clearBuffer();
    }

    private void tokenNumericoFloat(BufferedReader arquivoUsuario) { //Metodo para classificação de Token Numerico do tipo Float.
        writeToken();
        lookAHead(arquivoUsuario);
        while (true) {
            if (LOOKAHEAD == 65535 && (BUFFER.charAt(BUFFER.length() - 1) == '.')) {                
                characterInvalid("Float Mal Formado");
                System.exit(0);
            } else if (LOOKAHEAD < 48 && LOOKAHEAD > 57) {                
                characterInvalid("Float Mal Formado");
                System.exit(0);
            } else if (LOOKAHEAD >= 48 && LOOKAHEAD <= 57) {
                writeToken();
                lookAHead(arquivoUsuario);
            } else {
                break;
            }
        }
        TOKENS.addFirst("70");
        TOKENS.addFirst(BUFFER);
        clearBuffer();
    }

    private void tokenID(BufferedReader arquivoUsuario) { //Metodo para a classificação de tokens do tipo Identificadores.
        while (Character.isLetterOrDigit(LOOKAHEAD) || LOOKAHEAD == 95) {
            writeToken();
            lookAHead(arquivoUsuario);
        }
        if (tokenReserved()) {
            BUFFER = "";
        } else {
            TOKENS.addFirst("10");
            TOKENS.addFirst(BUFFER);
            clearBuffer();
        }
    }

    private boolean tokenReserved() { //Metodo para a verificação de tokens de identificadores que possam ser palavras reservadas na linguagem defina.
        switch (BUFFER) {
            case "main":
                TOKENS.addFirst("50");
                TOKENS.addFirst(BUFFER);
                return true;
            case "if":
                TOKENS.addFirst("51");
                TOKENS.addFirst(BUFFER);
                return true;
            case "else":
                TOKENS.addFirst("52");
                TOKENS.addFirst(BUFFER);
                return true;
            case "while":
                TOKENS.addFirst("53");
                TOKENS.addFirst(BUFFER);
                return true;
            case "do":
                TOKENS.addFirst("54");
                TOKENS.addFirst(BUFFER);
                return true;
            case "for":
                TOKENS.addFirst("55");
                TOKENS.addFirst(BUFFER);
                return true;
            case "int":
                TOKENS.addFirst("56");
                TOKENS.addFirst(BUFFER);
                return true;
            case "float":
                TOKENS.addFirst("57");
                TOKENS.addFirst(BUFFER);
                return true;
            case "char":
                TOKENS.addFirst("58");
                TOKENS.addFirst(BUFFER);
                return true;
        }
        return false;
    }

    private void tokenChar(BufferedReader arquivoUsuario) { //Metodo para criação de tokens do tipo Character.
        writeToken();
        lookAHead(arquivoUsuario);
        if (Character.isLetterOrDigit(LOOKAHEAD)) {//BUFFER = '+ Digito ou Letra
            writeToken();
            lookAHead(arquivoUsuario);
            if (LOOKAHEAD == 39) { //BUFFER = 'Digito ou Letra'
                writeToken();
                TOKENS.addFirst("80");
                TOKENS.addFirst(BUFFER);
                clearBuffer();
                lookAHead(arquivoUsuario);
            }
        } else {            
            characterInvalid("Charactere mal formado");
            System.exit(0);
        }
    }

    private void tokenSpecial(BufferedReader arquivoUsuario) {//Metodo para classificação de tokens do tipo Especial.
        writeToken();
        lookAHead(arquivoUsuario);
        switch (BUFFER) {
            case ";":
                TOKENS.addFirst("40");
                TOKENS.addFirst(BUFFER);
                break;
            case ",":
                TOKENS.addFirst("41");
                TOKENS.addFirst(BUFFER);
                break;
            case "{":
                TOKENS.addFirst("42");
                TOKENS.addFirst(BUFFER);
                break;
            case "}":
                TOKENS.addFirst("43");
                TOKENS.addFirst(BUFFER);
                break;
            case "(":
                TOKENS.addFirst("44");
                TOKENS.addFirst(BUFFER);
                break;
            case ")":
                TOKENS.addFirst("45");
                TOKENS.addFirst(BUFFER);
                break;
        }
        clearBuffer();
    }

    private void tokenAritmetico(BufferedReader arquivoUsuario) {//Metodo para classificação de tokens do tipo Aritmetico .
        writeToken();
        lookAHead(arquivoUsuario);
        switch (BUFFER) {
            case "=":
                if (LOOKAHEAD == '=') {
                    tokenRelacional(arquivoUsuario);
                } else {
                    TOKENS.addFirst("30");
                    TOKENS.addFirst(BUFFER);
                }
                break;
            case "+":
                TOKENS.addFirst("31");
                TOKENS.addFirst(BUFFER);
                break;
            case "-":
                TOKENS.addFirst("32");
                TOKENS.addFirst(BUFFER);
                break;
            case "*":
                TOKENS.addFirst("33");
                TOKENS.addFirst(BUFFER);
                break;
            case "/": //Caso o proximo caracter após o '/' seja outra '/' ou um '*', ele será identificado como um comentario e irá para o respectivo metodo de classificação.
                if (LOOKAHEAD == '/') {
                    comment(arquivoUsuario);
                    return;
                } else if (LOOKAHEAD == '*') {
                    comment(arquivoUsuario);
                    return;
                } else {
                    TOKENS.addFirst("34");
                    TOKENS.addFirst(BUFFER);
                }
                break;
        }
        clearBuffer();
    }

    private void comment(BufferedReader arquivoUsuario) {//Metodo para verificação de comentarios.
        if (LOOKAHEAD == '/') { //Caso seja um comentario de linha unica.
            while (true) {
                if (LOOKAHEAD == 13) {// quebra de linha '\n'
                    clearBuffer();
                    return;
                } else if (LOOKAHEAD == 65535) { //EOF
                    clearBuffer();
                    return;
                } else {
                    lookAHead(arquivoUsuario);
                }
            }
        } else { //Caso seja um comentario de quebra de linha.
            while (true) {
                lookAHead(arquivoUsuario);
                if (LOOKAHEAD == 65535) {                    
                    characterInvalid("Comentario mal formado");
                    System.exit(0);
                } else if (LOOKAHEAD == '*') {
                    lookAHead(arquivoUsuario);
                    if (LOOKAHEAD == '/') {
                        lookAHead(arquivoUsuario);
                        clearBuffer();
                        return;
                    }
                }
            }
        }
    }

    private void tokenRelacional(BufferedReader arquivoUsuario) { //Metodo de classificação para tokens do tipo Relacional
        writeToken();
        lookAHead(arquivoUsuario);
        switch (BUFFER) {
            case "==":
                TOKENS.addFirst("20");
                TOKENS.addFirst(BUFFER);
                break;
            case ">":
                if (LOOKAHEAD == '=') {
                    writeToken();
                    TOKENS.addFirst("21");
                    TOKENS.addFirst(BUFFER);
                } else {
                    TOKENS.addFirst("22");
                    TOKENS.addFirst(BUFFER);
                }
                break;
            case "<":
                if (LOOKAHEAD == '=') {
                    writeToken();
                    TOKENS.addFirst("23");
                    TOKENS.addFirst(BUFFER);
                } else {
                    TOKENS.addFirst("24");
                    TOKENS.addFirst(BUFFER);
                }
                break;
            case "!":
                if (LOOKAHEAD == '=') {
                    writeToken();
                    TOKENS.addFirst("25");
                    TOKENS.addFirst(BUFFER);
                } else {
                    characterInvalid("Token relacional mal formado.");
                }
                break;
        }
        lookAHead(arquivoUsuario);
        clearBuffer();
    }

       private void characterInvalid(String mensagem) { //Metodos informativos caso haja um caracter invalido no Arquivo enviado pelo usuario. O metodo printa a linha, coluna e informa qual foi o ultimo token lido e seu tipo.
        String token = TOKENS.pop(), tipo = TOKENS.pop();
        System.err.println("ERRO na linha " + LINE + ", coluna " + COLUMN + ", ultimo token lido:  [Tipo = " + tipo + "],[Token = '" + token + "' ] " + mensagem);
        System.exit(0);
    }
       
    private void printTokens() { //Metodo para printar todos os tokens e seus respectivos tipos.
        while (TOKENS.peek() != null) {
            if (TOKENS.peek() != null) {
                System.out.println(TOKENS.remove() + " " + TOKENS.remove());
            } else {
                System.out.println("");
            }
        }
    }

}
