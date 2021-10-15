var gulp = require('gulp');
var build = require('./build');
var replace = require('gulp-replace');
var func = require('./helpers');

const PREG_APIURL = new RegExp(/["|'](inc\/api.*?)["|']/);

var apiUrlCallback = function(full, part) {
  return full.replace(part, build.config.path.demo_api_url + part);
};

// Gulp task to find api path and convert to absolute url
gulp.task('apiurl', function() {
  build.config.dist.forEach(function(path) {
    var output = '';
    if (path.indexOf('**') !== -1) {
      func.getDemos().forEach(function(demo) {
        output = path;
        output = output.replace('**', demo);
        gulp.src(output + '/**/*js').pipe(replace(PREG_APIURL, apiUrlCallback)).pipe(gulp.dest(output));
      });
    } else {
      output = path;
      gulp.src(output + '/**/*js').pipe(replace(PREG_APIURL, apiUrlCallback)).pipe(gulp.dest(output));
    }
  });
});