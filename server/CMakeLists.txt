cmake_minimum_required(VERSION 3.0)
project(server)

set(CMAKE_CXX_STANDARD 14)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")

add_executable(server protbuf/messages.pb.cc main.cpp)

target_link_libraries(server -pthread -I/usr/local/include -L/usr/local/lib -lprotobuf -pthread -lpthread)