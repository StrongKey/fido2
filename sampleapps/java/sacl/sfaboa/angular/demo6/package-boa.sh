#!/bin/bash
npm install
ng build
cp modernizr.js dist/
tar czf sfaboa-ui-dist.tar.gz dist/
mv sfaboa-ui-dist.tar.gz ../
