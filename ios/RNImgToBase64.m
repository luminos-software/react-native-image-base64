
#import "RNImgToBase64.h"

@implementation RNImgToBase64

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

#pragma mark getBase64String
RCT_EXPORT_METHOD(getBase64String:(NSURL*)url
    token:(NSString*)token
    compression:(double)compression
    resolve:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject
) {
    dispatch_async(dispatch_queue_create("image_processing", 0), ^{
        NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
        NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration delegate:nil delegateQueue:nil];
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                               cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                           timeoutInterval:60.0];

        
        [request addValue:[NSString stringWithFormat:@"Bearer %@", token] forHTTPHeaderField:@"Authorization"];
        [request setHTTPMethod:@"GET"];

        dispatch_async(dispatch_get_main_queue(), ^{
            NSURLSessionDataTask *getDataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                NSHTTPURLResponse* respHttp = (NSHTTPURLResponse*) response;
                if (respHttp.statusCode != 200){
                    reject([NSString stringWithFormat: @"%lu", (long)error.code], error.localizedDescription, error);
                    return;
                }
                
                UIImage *image = [UIImage imageWithData:data];
                NSData *pngData = UIImageJPEGRepresentation(image, compression);
                NSLog(@"[EMANUEL]%@", respHttp);
                
                NSDictionary *responseBody = @{@"data": [NSString stringWithFormat:@"data:image/jpeg;base64,%@",[pngData base64EncodedStringWithOptions:0]]};
                resolve(responseBody);
                
            }];
            
            [getDataTask resume];
        });
    });
}

@end
