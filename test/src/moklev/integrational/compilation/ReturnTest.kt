package moklev.integrational.compilation

import moklev.compiler.compilation.DiagnosticCompilationErrors.MissingReturnError
import org.junit.Test

/**
 * @author Moklev Vyacheslav
 */
class ReturnTest : CompilationTestBase() {
    @Test
    fun testSimpleReturn() {
        runTestSucceeded("""
            fun foo(): int64 {
                return 1;
            }
        """)
        runTestError("""
            fun foo(): int64 {
            }
        """, MissingReturnError("foo"))
    }
    
    @Test
    fun testSequenceReturn() {
        runTestSucceeded("""
            fun foo(): int64 {
                var x: int64;
                x = 1;
                return x;
            }
        """)
        runTestError("""
            fun foo(): int64 {
                var x: int64;
                x = 1;
            }
        """, MissingReturnError("foo"))
    }
    
    @Test
    fun testIfReturn() {
        runTestSucceeded("""
            fun foo(): int64 {
                if (1 < 2) {
                    return 1;
                }
                return 2;
            }
        """)
        runTestSucceeded("""
            fun foo(): int64 {
                if (1 < 2) {
                    return 1;
                } else {
                    return 2;
                }
            }
        """)
        runTestSucceeded("""
            fun foo(): int64 {
                if (1 < 2) {}
                return 2;
            }
        """)
        runTestError("""
            fun foo(): int64 {
                if (1 < 2) {
                    return 1;
                }
            }
        """, MissingReturnError("foo"))
        runTestError("""
            fun foo(): int64 {
                if (1 < 2) {
                    return 1;
                } else {}
            }
        """, MissingReturnError("foo"))
        runTestError("""
            fun foo(): int64 {
                if (1 < 2) {}
            }
        """, MissingReturnError("foo"))
    }
    
    @Test
    fun testWhileReturn() {
        runTestSucceeded("""
            fun foo(): int64 {
                while (1 < 2) {}
                return 1;
            }
        """)
        runTestSucceeded("""
            fun foo(): int64 {
                while (1 < 2) {return 1;}
                return 2;
            }
        """)
        runTestError("""
            fun foo(): int64 {
                while (1 < 2) {
                    return 1;
                }
            }x
        """, MissingReturnError("foo"))
        runTestError("""
            fun foo(): int64 {
                while (1 < 2) {}
            }
        """, MissingReturnError("foo"))
    }
    
    @Test
    fun testComplexReturn() {
        runTestError("""
            fun foo(): int64 {
                var x: int64;
                x = 1;
                while (x < 10) {
                    if (x < 5) {
                        x = x + 2;
                    } else {
                        x = x + 3;
                        return x;
                    }
                }
            }
        """, MissingReturnError("foo"))
        runTestSucceeded("""
            fun foo(): int64 {
                if (1 < 2) {
                    if (1 < 2) {
                        if (1 < 2) {
                            if (1 < 2) {
                                return 1;
                            } else {
                                return 2;
                            }
                        } else {
                            if (1 < 2) {
                                return 3;
                            } else {
                                return 4;
                            }
                        }        
                    } else {
                        if (1 < 2) {
                            if (1 < 2) {
                                return 5;
                            } else {
                                return 6;
                            }
                        } else {
                            if (1 < 2) {
                                return 7;
                            } else {
                                return 8;
                            }
                        }
                    }
                } else {
                    if (1 < 2) {
                        if (1 < 2) {
                            if (1 < 2) {
                                return 9;
                            } else {
                                return 10;
                            }
                        } else {
                            if (1 < 2) {
                                return 11;
                            } else {
                                return 12;
                            }
                        }
                    } else {
                        if (1 < 2) {
                            if (1 < 2) {
                                return 13;
                            } else {
                                return 14;
                            }
                        } else {
                            if (1 < 2) {
                                return 15;
                            } else {
                                return 16;
                            }
                        }
                    }
                }
            }
        """)
    }
}