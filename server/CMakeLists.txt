cmake_minimum_required(VERSION 3.0)
project(server)

set(CMAKE_CXX_STANDARD 14)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")

add_executable(server main.cpp)