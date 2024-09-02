package cn.yourscrm.mono.auth.adaptor.jwt;

import org.springframework.data.repository.ListCrudRepository;

public interface SignKeyEntityRepo extends ListCrudRepository<SignKeyEntity, Long> {
}
