package com.oasis.web_model.response.success.entry_point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntryPointResponse {

    private String username;
    private String name;
    private String photo;
    private String role;

}