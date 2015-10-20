var gulp = require('gulp');
var webpack = require('webpack-stream');
var webpackConfig = require('./webpack.config.js');

gulp.task('build', function() {
    return gulp.src('')
        .pipe(webpack(webpackConfig))
        .pipe(gulp.dest(''));
});

gulp.task('watch', function() {
    gulp.watch('./web/assets/**/*', ['build']);
});

gulp.task('default', ['build', 'watch']);
