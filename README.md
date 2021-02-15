# Sellics - Search volume estimate

## Assumptions
* The Amazon autocomplete API mentioned in the assignment is not used. Instead, the https://completion.amazon.co.uk/api/2017/suggestions
  API, which is used by Amazon in search auto complete is used.
* The defined SLA for estimate API is 10s. On the worst case, a maximum of 6 calls are made to the Amazon autocomplete
  API. Each call to Amazon's API can take up to 1s, for estimate API to meet its SLA.
* Exact match of input keyword with suggestion response from API is only considered a valid match. Contains match is not considered a valid match.

## Algorithm
Given an input keyword, the keyword will be broken down into multiple tokens. These tokens will be queried with the 
Amazon autocomplete API sequentially. Based on the prior response, next token to be queried will be decided. 

### Matching algorithm
Let's assume the input keyword - 'puma'.

The following will be the request param in the calls made to the autocomplete API.

* p  -> Response does not have any suggestion with exact match to keyword.
* pu -> Response has an exact match to the keyword in suggestions.

The above keyword took 2 calls to find an exact match and was found in 5th position of the response. Both of these 
attributes will have an impact on the score (position has a much lower weightage).

The below example shows the sequence of tokens that will be used to query the API, until a match is found.

* p
* pu
* pum
* puma

For an input keyword with two words - 'coca cola'

* c
* co
* coc
* coca
* coca c
* coca co

#### Partial match cases
keyword = 'iphone charger cables'

* i -> [ios, iphone, iphone 5s, iphone 12,..]
* iphone c -> [iphone case, iphone charger cables, iphone charger 20w,..]

Match found in second call's of second position.

keyword = 'microsoft windows vista'

* m   -> [masks, massage gun,..]
* mi  -> [mini fridge, mi band,..]
* mic -> [microwave, microsoft surface,..]
* microsoft w -> [microsoft windows 10, microsoft windows 10 pro 64 bit,..]
* microsoft windows v -> [microsoft windows vista,..]

Match found in fifth call's first position.

#### Why do we need a partial match case?
Let's assume the keyword 'iphone charger cables'

We did not receive an exact match when we queried for 'i'. But, we received a number of responses which were prefixed with
'iphone'. If we continued our search in a linear fashion assuming, as there is no exact match, we will append the 
query param as i..p..h..o..n..e. in subsequent calls, we will eventually get a match by the time we reach 'iphone c'. 
But, our score would be heavily impacted because of the number of calls we had made earlier to reach this point.

This is avoided by considering a partial match case. Although, 'i' query couldn't do an exact match with 'iphone charger cables',
we get a partial match with 'iphone'. Hence, the subsequent call made to the API will be 'iphone c' and not 'ip'. This is done
to mitigate the effects of a powerful prefix, which shadows the input keyword as the prefix has a higher search volume and 
dominates the suggestions. 

#### Why a sequential query to auto complete API? Why not a parallel one?
Since our partial match logic depends on the outcome of prior response to decide the query param for next request, the 
logic had to be made sequential. A parallel search would have only been possible if our tokenization was predictable.

### Scoring logic
There are 2 factors that contribute to the score.

* The number of calls made to the API before getting a match in the response - higher weightage.
* The position (index) of the match in the response - lower weightage.
  
Score is identified to have an exponential relation with the number of calls made. The following equation is used to 
compute the search volume estimate

    y = 100 - x^2;
where y is the score   
x is the number of calls made to auto complete API + index in match/10.
  
#### Why exponential scoring over a linear scoring?
The results coming by querying auto complete API with just 1 or 2 characters has significantly greater search volume than the ones coming with 4 or 5 characters being queried.
The latter set required a major slice of its keyword queried to come up in the response, which signifies it is not being searched widely. 
Hence, an exponential scoring algorithm is applicable here than a linear scoring approach. 

#### Why a max of 6 calls to autocomplete API?
Any search keyword that gets a response after 6 calls, means the product has a negligible search volume. So the number of 
autocomplete calls are capped at 6. If there is no match until the last call, a score of zero is returned. 
As per the scoring equation, an x value greater than 5 would return negative values. The acceptable range of x is 0 < x < 5.0 .

## Hint - Analysis

### Hint : The order of the 10 returned keywords is comparatively insignificant!

The order of the search results in page 1 or 2, does not have much of an impact on the search volume score. This is because the available 
set of suggestions are competing against a huge set of records to be picked up and returned in the top 10 suggestions. The same can be seen in the
scoring logic where the score doesn't deviate much for page 1 or 2 matches.

But the same order of search results, does have significant impact in results of page 5 or 6. 
This is because the search query is well constructed at this point and there are much fewer competing records. Here the search volume also comes into
picture, on which record to be displayed first, and which of the records follow them.

If the logic did not give weightage to the index of match, then all the estimates will only be matched against just 6 points. 
The best one getting a score of 5 while the worst one would get a 0. In order to have a better distribution of score in the lower
score spectrum, it was necessary to add weightage to the positions of match.


## Accuracy of outcome
* Accuracy on keywords with no spaces - High
* Accuracy on keywords with spaces and no partial match - High
* Accuracy on keywords with spaces and with partial match - A deviation of up to 10-15% in score is possible

Contains match is not addressed and is discussed in the Scope for improvement section below.

## Scope for improvement

### A match is always an exact match
Let's take the keyword - 'iphone charger'  
API response gives us - 'iphone charger cables'

Although the keyword is entirely contained within the suggestion from API, we did not mark it as a complete match and went
ahead searching until we find an exact match. And in many such cases, the match was always a contains match and not an exact 
match. I don't have a solution yet to this issue.

### Different forms of a word
Let's take the keyword - 'iphone charger cable'  
API response gives us - 'iphone charger cables'

With this example, the exact match issue would not let us match these two Strings. But, unlike the exact match case, this 
is still a perfect match to a human eye, but the word cable is in different forms. More research in the direction of 
Stemming or Lemmatization can give us a solution to match various forms of words, if this is a relevant use case.

## Environment
* JDK-1.8
* Apache Maven

To start the application, please run KeywordServiceApplication.java file.   
Hit http://localhost:8080/estimate?keyword={query}