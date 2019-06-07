var gulp = require('gulp');
var gutil = require('gulp-util');
var sequence = require('run-sequence');
var build = require('./build');
var func = require('./helpers');

// merge with default parameters
var args = Object.assign({'prod': false}, gutil.env);

if (args.prod !== false) {
  // force disable debug for production
  build.config.debug = false;
  build.config.compile = {
    'jsUglify': true,
    'cssMinify': true,
    'jsSourcemaps': false,
    'cssSourcemaps': false,
  };
}

// task to bundle js/css
gulp.task('build-bundle', function(cb) {
  // build by demo, leave demo empty to generate all demos
  if (build.config.demo !== '') {
    for (var demo in build.build.demo) {
      if (!build.build.demo.hasOwnProperty(demo)) continue;
      if (build.config.demo !== demo) {
        delete build.build.demo[demo];
      }
    }
  }
  func.objectWalkRecursive(build.build, function(val, key) {
    if (typeof val.src !== 'undefined') {
      if (typeof val.bundle !== 'undefined') {
        func.bundle(val);
      }
      if (typeof val.output !== 'undefined') {
        func.output(val);
      }
    }
  });
  cb();
});

// entry point
gulp.task('default', ['clean'], function(cb) {
  // clean first and then start bundling
  return sequence(['build-bundle'], cb);
});