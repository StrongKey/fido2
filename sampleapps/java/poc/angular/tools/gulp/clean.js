var gulp = require('gulp');
var del = require('del');
var build = require('./build');
var func = require('./helpers');

// task to clean and delete dist directory content
var getPaths = function(outPaths) {
	if (typeof outPaths === 'undefined') outPaths = [];
	var paths = [];
	var outputs = build.config.dist;
	outputs.forEach(function(output) {
		// has demo placeholder
		if (output.indexOf('**') !== -1) {
			func.getDemos().forEach(function(demo) {
				paths.push(output.replace('**', demo));
			});
		} else {
			if (outPaths.length === 0) {
				paths.push(output);
			}
			outPaths.forEach(function(path) {
				paths.push(output + '/' + path);
			});
		}
	});

	var realpaths = [];
	paths.forEach(function(path) {
		realpaths.push(path + '/*');
		realpaths.push('!' + path + '/lib');
	});

	return realpaths;
};

gulp.task('clean', function() {
	return del(getPaths(), {force: true});
});