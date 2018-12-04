#include <stdio.h>
#include <stdint.h>

void format_int64(int64_t value) {
    printf("Int64[%lld]", value);
}

void format_bool(int value) {
    if (value) {
        printf("Boolean[true]");
    } else {
        printf("Boolean[false]");
    }
}

$test_definition

int main() {
    $test_call
    return 0;
}