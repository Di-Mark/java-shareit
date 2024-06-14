package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, long userId){
        return post("",userId,itemDto);
    }

    public ResponseEntity<Object> patchItem(ItemDto itemDto,long itemId,Long userId){
        return patch("/" + itemId,userId,itemDto);
    }

    public ResponseEntity<Object> getItem(Long itemId,long userId){
        return get("/" + itemId,userId);
    }

    public ResponseEntity<Object> addComment(Comment comment,Long itemId,long userId){
        return post("/" + itemId + "/comment",userId,comment);
    }

    public ResponseEntity<Object> getItemsListForUser(long userId,Integer from, Integer size){
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}",userId,parameters);
    }

    public ResponseEntity<Object> searchItemsForText(String text,Integer from,Integer size,long userId){
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}",userId,parameters);
    }
}
