# Whisper v3 Java Lib using DJL

Library to run inference of [Whisper v3](https://github.com/openai/whisper) in Java using [DJL](https://djl.ai/). 
This implementation is based on the [huggingface Python implementation of Whisper.](https://huggingface.co/openai/whisper-large-v3)
Currently only runs on GPU. 

The library has the ability to run inference on the GPU in Java out of the box.

Alternatives:
- [whisper.cpp](https://github.com/ggerganov/whisper.cpp) to run Whisper with C++
- [whisper-jni](https://github.com/GiviMAD/whisper-jni) (a JNI wrapper for whisper.cpp)

## Installation

First, follow the installation instructions for the [DJL PyTorch engine](https://djl.ai/engines/pytorch/pytorch-engine/#installation).

For GPU support, you also need to ensure [CUDA](https://developer.nvidia.com/cuda-toolkit) is installed on your system and included in the path.
You will need a CUDA version that matches the PyTorch version of your chosen DJL PyTorch engine. To see which DJL PyTorch engine version supports
which PyTorch library version, [see here](https://djl.ai/engines/pytorch/pytorch-engine/#supported-pytorch-versions).

## Usage example

Add the following to your pom file: 

```xml
<!-- DIVISIO Repo with the lib and model jars (not available on maven central due to size restrictions) -->
TODO: DIVISIO repo

<!-- Library dependency to run the mode -->
TODO: lib dependency

<!-- Dependency to the traced whisper 3 GPU model -->
TODO: model dependency
```

Create a demo class like this:

```java
package divisio.whisper;

import divisio.whisper.token.Whisper3Language;
import divisio.whisper.token.WhisperToken;

import java.util.Arrays;

public class WhisperDemo {

    public static void main(String[] args) throws Exception {
        final String filePath = args[0];

        try (Whisper3 whisper = Whisper3.instance()) {
            WhisperResult result = whisper.task()
                    .language(Whisper3Language.AUTO)
                    .transcribe(filePath)
                    .withTimestamps()
                    .execute();

            System.out.println("raw token ids: " + Arrays.toString(result.tokens().stream().mapToLong(WhisperToken::getTokenId).toArray()));
            System.out.println("raw text: " + result.rawText());
            System.out.println("clean: " + result.text());
        }
    }
}
```

And start the program with a parameter pointing to an audio file like `/path/to/my_audio_file.wav`.

**Initiating Whisper is expensive, so instances should be reused**, e.g. by instantiating them as a spring bean singleton.
Additionally, the first tasks might take a little bit longer than usual, due to internal warm-ups.

## Credits

This work is based upon the huggingface version of whisper3 (https://huggingface.co/openai/whisper-large-v3/blob/main/README.md)
by OpenAI. It is a traced version of that model, all JAVA code has been rewritten from scratch. We used 
the original Python code as a reference.

## License

This library is licensed under the Apache 2.0 license (see LICENSE). 

