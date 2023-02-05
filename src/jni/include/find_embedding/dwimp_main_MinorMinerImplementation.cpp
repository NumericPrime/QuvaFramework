// Copyright 2017 - 2020 D-Wave Systems Inc.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

#include <iostream>
#include "find_embedding.hpp"
#include <jni.h>
#include "dwas_main_MinorMinerImplementation.h"

class MyCppInteractions : public find_embedding::LocalInteraction {
  public:
    bool _canceled = false;
    void cancel() { _canceled = true; }

  private:
    void displayOutputImpl(int, const std::string& mess) const override { std::cout << mess << std::endl; }
    void displayErrorImpl(int, const std::string& mess) const override { std::cerr << mess << std::endl; }
    bool cancelledImpl() const override { return _canceled; }
};
JNIEXPORT jobjectArray JNICALL Java_dwas_main_MinorMinerImplementation_find_1Embedding
  (JNIEnv * env, jobject inst, jint l1, jint l2, jintArray arr1, jintArray arr2, jintArray arr3, jintArray arr4){
    jint* arr1_=(env)->GetIntArrayElements(arr1,0);
    jint* arr2_=(env)->GetIntArrayElements(arr2,0);
    jint* arr3_=(env)->GetIntArrayElements(arr3,0);
    jint* arr4_=(env)->GetIntArrayElements(arr4,0);
    jsize al1=(env->GetArrayLength(arr1));
    jsize al2=(env->GetArrayLength(arr3));
    std::vector<int> vec1(al1);
    std::vector<int> vec2(al1);
    std::vector<int> vec3(al2);
    std::vector<int> vec4(al2);
    for(int i=0;i<al1;i++) vec1[i]=arr1_[i];
    for(int i=0;i<al1;i++) vec2[i]=arr2_[i];
    for(int i=0;i<al2;i++) vec3[i]=arr3_[i];
    for(int i=0;i<al2;i++) vec4[i]=arr4_[i];
    graph::input_graph triangle(l1, vec1, vec2);
    graph::input_graph square(l2, vec3, vec4);
    find_embedding::optional_parameters params;
    params.localInteractionPtr.reset(new MyCppInteractions());

    std::vector<std::vector<int>> chains;
    findEmbedding(triangle, square, params, chains);
    /*if (find_embedding::findEmbedding(triangle, square, params, chains)) {
        for (auto chain : chains) {
            for (auto var : chain) std::cout << var << " ";
            std::cout << std::endl;
        }
    } else {
        std::cout << "Couldn't find embedding." << std::endl;
    }*/
    //std::string ret="test";
   jobjectArray joa=(env)->NewObjectArray(chains.size(),env->FindClass("[I"),NULL);
   for(int i=0;i<chains.size();i++) {
	jintArray ia=env->NewIntArray(chains[i].size());
	jint doneintarray[chains[i].size()];
	for(int j=0;j<chains[i].size();j++) doneintarray[j]=chains[i][j];
	env->SetIntArrayRegion(ia,0,chains[i].size(),doneintarray);
    	env->SetObjectArrayElement(joa,i,ia);
    }
    //return (env)->NewStringUTF(ret.c_str());
   return joa;
}