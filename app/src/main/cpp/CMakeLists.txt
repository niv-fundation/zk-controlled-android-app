cmake_minimum_required(VERSION 3.22.1)

project("simple")

include_directories(include)

link_directories(lib)

add_library(rapidsnark SHARED IMPORTED)
set_target_properties(rapidsnark PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/lib/librapidsnark.so)

add_library(auth SHARED IMPORTED)
set_target_properties(auth PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/lib/libwitnesscalc_IdentityAuth.so)

add_library(${CMAKE_PROJECT_NAME} SHARED
        simple.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log rapidsnark auth)