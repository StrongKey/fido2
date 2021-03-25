#!/bin/bash
npm install
ng build
cp modernizr.js dist/
tar czf sfakma-ui-dist.tar.gz dist/
mv sfakma-ui-dist.tar.gz ../
