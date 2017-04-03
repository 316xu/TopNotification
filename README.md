# TopNotification

## Add TopNotification to your project

Your can use it by gradle
```
compile 'cn.xujifa.topnotification:topnotification:0.9'
```

## Using the library

Simple samle:

```
TopNotification.Builder builder = new TopNotification.Builder(this);

builder.setMessage("Duration")
        .setDuration(3000)
        .setIsProgressBarVisible(true)
        .create()
        .show();
```

## Example

![](https://raw.githubusercontent.com/jifaxu/TopNotification/master/raw/sample1.gif)

![](https://raw.githubusercontent.com/jifaxu/TopNotification/master/raw/sample2.gif)

![](https://raw.githubusercontent.com/jifaxu/TopNotification/master/raw/sample3.gif)


## License
```
MIT License

Copyright (c) 2017 许矶法

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
