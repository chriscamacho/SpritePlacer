#!/bin/bash

rm -f *.jar

gdxdist="../../libgdx/dist/"

jars=(
"gdx.jar"
"gdx-natives.jar"
"gdx-backend-lwjgl.jar"
"gdx-backend-lwjgl-natives.jar"
"extensions/gdx-box2d/gdx-box2d.jar"
"extensions/gdx-box2d/gdx-box2d-natives.jar"
)

for jar in ${jars[@]}
do
    echo $jar
    cp $gdxdist$jar .
done
