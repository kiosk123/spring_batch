# 18. ItemProcessor Interface 구조 이해

![.](./img/1.png)
![.](./img/2.png)

ItemReader에서 null을 리턴하면 chunk 처리가 끝났다는 의미  
ItemProcessor에서 null을 리턴하면 ItemWriter에 해당 아이템을 처리하지 않겠다는 의미  

## 위의 처리과정 예제
