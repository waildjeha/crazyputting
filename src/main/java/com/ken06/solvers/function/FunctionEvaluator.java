package com.ken06.solvers.function;

public class FunctionEvaluator {

    /**
     * A functional interface representing our compiled mathematical formula.
     */
    public interface CompiledFunction {
        double eval(double x, double y);
    }

    /**
     * Parses the string ONCE and returns an executable CompiledFunction.
     * 
     * @param str the mathematical expression to compile (e.g. "0.25*sin(x/2) + y")
     * @return an optimized CompiledFunction that you can call repeatedly
     */
    public static CompiledFunction compile(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            CompiledFunction parse() {
                nextChar();
                CompiledFunction x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            CompiledFunction parseExpression() {
                CompiledFunction x = parseTerm();
                for (;;) {
                    if (eat('+')) { 
                        CompiledFunction a = x, b = parseTerm(); 
                        x = (varX, varY) -> a.eval(varX, varY) + b.eval(varX, varY); 
                    }
                    else if (eat('-')) { 
                        CompiledFunction a = x, b = parseTerm(); 
                        x = (varX, varY) -> a.eval(varX, varY) - b.eval(varX, varY); 
                    }
                    else return x;
                }
            }

            CompiledFunction parseTerm() {
                CompiledFunction x = parseFactor();
                for (;;) {
                    if (eat('*')) { 
                        CompiledFunction a = x, b = parseFactor(); 
                        x = (varX, varY) -> a.eval(varX, varY) * b.eval(varX, varY); 
                    }
                    else if (eat('/')) { 
                        CompiledFunction a = x, b = parseFactor(); 
                        x = (varX, varY) -> a.eval(varX, varY) / b.eval(varX, varY); 
                    }
                    else return x;
                }
            }

            CompiledFunction parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) { 
                    CompiledFunction a = parseFactor(); 
                    return (varX, varY) -> -a.eval(varX, varY); 
                }

                CompiledFunction x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                }
                else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    double val = Double.parseDouble(str.substring(startPos, this.pos));
                    // Return a function that just returns the constant number
                    x = (varX, varY) -> val; 
                }
                else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String name = str.substring(startPos, this.pos);

                    // Map variables to the lambda inputs
                    if (name.equals("x")) return (varX, varY) -> varX;
                    if (name.equals("y")) return (varX, varY) -> varY;

                    if (eat('(')) {
                        CompiledFunction arg = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + name);
                        x = (varX, varY) -> applyFunction(name, arg.eval(varX, varY));
                    } else {
                        throw new RuntimeException("Unknown identifier: " + name);
                    }
                }
                else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) { 
                    CompiledFunction a = x, b = parseFactor(); 
                    x = (varX, varY) -> Math.pow(a.eval(varX, varY), b.eval(varX, varY)); 
                }

                return x;
            }

            double applyFunction(String func, double arg) {
                return switch (func) {
                    case "sin" -> Math.sin(arg);
                    case "cos" -> Math.cos(arg);
                    case "tan" -> Math.tan(arg);
                    case "sqrt" -> Math.sqrt(arg);
                    case "log" -> Math.log(arg);
                    case "exp" -> Math.exp(arg);
                    default -> throw new RuntimeException("Unknown function: " + func);
                };
            }

        }.parse();
    }
}