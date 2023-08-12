package org.gaurav;

import org.gaurav.chatGpt.ChatGpt;
import org.gaurav.linkedin.Linkedin;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        usage:
            linkedin [-t TEXT] [-a ARTICLE_URL] [-i IMAGE_PATH] [-dt DESCRIPTION_TEXT] [-tt TITLE_TEXT]

        Command line tool for posting on LinkedIn

        optional arguments:
            -h: show this help message and exit
            -t <Content | Text>: This flag specifies the text content of the post. It is required for all LinkedIn commands.
            -gt <ChatGPT_prompt>: This flag specifies the prompt for the ChatGPT model. This generates text content for the post.
            -a <Article_url>: This flag specifies the URL of an article to include in the post. It is optional and can be used with the -t flag for LinkedIn posts that include an url.
            -i <Image_path>: This flag specifies the path to an image file to include in the post. It is optional and can be used with the -t flag to post image with text.
            -dt <Description_text>: This flag specifies the description text for the post. It is optional and can be used with the -a or -i flags to provide additional information about the article or image.
                                    If no value is provided, it will default to an empty string ("").
            -tt <Title_text>: This flag specifies the title text for the post. It is optional and can be used with the -a or -i flags to provide a title for the article or image.
                                    If no value is provided, it will default to an empty string ("").

        Examples:
            - To post text content only:
                linkedin -t "Hello, this is my text-only post!"
            - To post an article URL with text, description text, and title text:
                linkedin -t "Check out this interesting article!" -a https://www.example.com/article
                linkedin -t "Check out this interesting article!" -a https://www.example.com/article -dt "This article provides some great insights" -tt "Interesting Article"
            - To post an image with description text and title text:
                linkedin -t "Here's an image I wanted to share!" -i /path/to/image.jpg
                linkedin -t "Here's an image I wanted to share!" -i /path/to/image.jpg -dt "This image shows a beautiful sunset" -tt "Beautiful Sunset"
         */
        if (args.length == 0) {
            System.err.println("No arguments provided");
            System.exit(1);
        }

        new Main().parseArgs(args);
    }
    

    public void parseArgs(String[] args) {
        String text = null;
        String articleUrl = null;
        String imagePath = null;
        String descriptionText = null;
        String titleText = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t" -> {
                    if(text != null) {
                        System.err.println("Warning: -gt flag is ignored when -t flag is used");
                        System.exit(1);
                    }
                    text = args[++i];
                }
                case "-gt" -> {
                    try {
                        if(text != null) {
                            System.err.println("Warning: -t flag is ignored when -gt flag is used");
                            System.exit(1);
                        }
                        text = new ChatGpt(args[++i]
                                .replace("\n", "\\n")).getResponse();
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error while getting response from chat GPT: \n" + e.getMessage());
                        System.exit(1);
                    }
                }
                case "-a" -> articleUrl = args[++i];
                case "-i" -> imagePath = args[++i];
                case "-dt" -> descriptionText = args[++i];
                case "-tt" -> titleText = args[++i];
                default -> {
                    System.err.println("Invalid argument: " + args[i]);
                    System.exit(1);
                }
            }
        }

        if (text == null) {
            System.err.println("Text is required");
            System.exit(1);
        }
        if (articleUrl != null && imagePath != null) {
            System.err.println("Only one of articleUrl or imagePath can be used at a time");
            System.exit(1);
        }
        boolean isShared = false;
        text = text.replace("\n", "\\n");
        try {
            Linkedin linkedin = new Linkedin();
            if (articleUrl != null) {
                if (descriptionText == null)
                    descriptionText = "";
                if (titleText == null)
                    titleText = "";
                isShared = linkedin.shareTextWithArticle(text, articleUrl, descriptionText, titleText);
            } else if (imagePath != null) {
                if (descriptionText == null)
                    descriptionText = "";
                if (titleText == null)
                    titleText = "";
                isShared = linkedin.shareTextWithImage(text, linkedin.getImageBinaryFile(imagePath), descriptionText, titleText);
            } else
                isShared = linkedin.shareText(text);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: \n" + e.getMessage());
        }
        if (isShared)
            System.out.println("Post shared successfully");
        else
            System.out.println("Post not shared");
    }
}