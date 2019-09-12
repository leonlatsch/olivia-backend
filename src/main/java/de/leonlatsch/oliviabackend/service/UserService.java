package de.leonlatsch.oliviabackend.service;

import de.leonlatsch.oliviabackend.dto.ProfilePicDTO;
import de.leonlatsch.oliviabackend.dto.PublicUserDTO;
import de.leonlatsch.oliviabackend.dto.Response;
import de.leonlatsch.oliviabackend.dto.UserDTO;
import de.leonlatsch.oliviabackend.entity.AccessToken;
import de.leonlatsch.oliviabackend.entity.User;
import de.leonlatsch.oliviabackend.repository.UserRepository;
import de.leonlatsch.oliviabackend.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static de.leonlatsch.oliviabackend.constants.JsonResponse.*;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessTokenService accessTokenService;

    private DatabaseMapper mapper = DatabaseMapper.getInstance();

    public Response getAllUsers(String accessToken) {
        if (!AdminManager.getAdminAccessToken().equals(accessToken)) {
            return null;
        }
        List<UserDTO> list = mapToTransferObjects(userRepository.findAll());

        for (UserDTO user : list) {
            rmProfilePic(user);
        }

        return new Response(200, OK, list);
    }

    public Response getPublicKey(int uid) {
        Optional<User> blob = userRepository.findPublicKeyByUid(uid);
        if (blob.isPresent()) {
            return new Response(200, OK, Base64.convertToBase64(blob.get().getPublicKey()));
        } else {
            return new Response(400, ERROR, null);
        }
    }

    public Response updatePublicKey(String accessToken, String publicKey) {
        Response response = new Response();
        int uid = accessTokenService.getUserForToken(accessToken);
        if (uid == -1) {
            response.setCode(401);
            response.setMessage(UNAUTHORIZED);
            response.setContent(null);
        } else {
            Optional<User> user = userRepository.findById(uid);
            if (user.isPresent()) {
                User newUser = user.get();
                newUser.setPublicKey(Base64.convertToBlob(publicKey));
                userRepository.saveAndFlush(newUser);
                response.setCode(200);
                response.setMessage(OK);
                response.setContent(null);
            } else {
                response.setCode(500); // Should never happen case
                response.setMessage(ERROR);
                response.setContent(null);
            }
        }
        return response;
    }

    public Response get(String accessToken) {
        Response response = new Response();
        int uid = accessTokenService.getUserForToken(accessToken);
        if (uid == -1) {
            response.setContent(204);
            response.setMessage(OK);
            response.setContent(null);
            return response;
        }
        Optional<User> user = userRepository.findById(uid);
        rmProfilePic(user);
        if (user.isPresent()) {
            response.setCode(200);
            response.setMessage(OK);
            response.setContent(mapper.mapToTransferObject(user.get()));
        } else {
            response.setContent(400);
            response.setMessage(ERROR);
            response.setContent(null);
        }
        return response;
    }

    public Response createUser(UserDTO user, String publicKey) {
        Response response = new Response();
        Optional<User> checkUser = userRepository.findByUsername(user.getUsername());
        if (checkUser.isPresent()) {
            response.setMessage(ERROR);
            response.setCode(400);
            response.setContent(null);
            return response;
        }

        Blob profilePic = ImageHelper.loadDefaultProfilePic();
        User entity = mapper.mapToEntity(user);

        int uid = CommonUtils.genUid();
        entity.setUid(uid);
        entity.setProfilePic(profilePic);
        entity.setProfilePicTn(ImageHelper.createThumbnail(profilePic));
        entity.setPublicKey(Base64.convertToBlob(publicKey));
        if (userRepository.saveAndFlush(entity) == null) {
            response.setMessage(ERROR);
            response.setContent(null);
            response.setCode(400);
            return response;
        }
        AccessToken token = new AccessToken();
        String rawToken = CommonUtils.genAccessToken(24);
        token.setUid(uid);
        token.setValid(true);
        token.setToken(rawToken);
        if (accessTokenService.saveAccessToken(token) == null) {
            response.setMessage(ERROR);
            response.setContent(null);
            response.setCode(400);
            return response;
        }

        response.setMessage(OK);
        response.setContent(rawToken);
        response.setCode(200);
        return response;
    }

    public Response deleteUser(String accessToken) {
        int uid = accessTokenService.getUserForToken(accessToken);
        if (uid == -1) {
            return new Response(400, ERROR, null);
        }
        userRepository.deleteById(uid);
        accessTokenService.disableAccessToken(accessToken);
        return new Response(200, OK, null);
    }

    public Response isUsernameFree(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        Response response = new Response();
        response.setCode(200);
        response.setContent(null);
        if (user.isPresent()){
            response.setMessage(TAKEN);
        } else {
            response.setMessage(FREE);
        }
        return response;
    }

    public Response isEmailFree(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        Response response = new Response();
        response.setCode(200);
        response.setContent(null);
        if (user.isPresent()){
            response.setMessage(TAKEN);
        } else {
            response.setMessage(FREE);
        }
        return response;
    }

    public Response updateUser(String accessToken, UserDTO userDTO) {
        int uid = accessTokenService.getUserForToken(accessToken);

        Response response = new Response();
        if (uid == -1) {
            response.setCode(400);
            response.setMessage(ERROR);
            response.setContent(null);
            return response;
        }
        User user = mapper.mapToEntity(userDTO);
        user.setUid(uid);

        Optional<User> dbUser = userRepository.findById(uid);
        if (dbUser.isPresent()) {
            if (user.getUsername() == null) {
                dbUser.get().setUsername(user.getUsername());
            }
            if (user.getEmail() == null) {
                dbUser.get().setEmail(user.getEmail());
            }
            if (user.getPassword() == null) {
                dbUser.get().setPassword(user.getPassword());
            }
            if (user.getProfilePic() == null) {
                dbUser.get().setProfilePic(user.getProfilePic());
            }
            if (user.getProfilePicTn() == null) {
                dbUser.get().setProfilePicTn(user.getProfilePicTn());
            }
            userRepository.saveAndFlush(dbUser.get());
            response.setCode(200);
            response.setMessage(OK);
            response.setContent(null);
            return response;
        } else {
            response.setCode(400);
            response.setMessage(ERROR);
            response.setContent(null);
            return response;
        }
    }

    public Response search(String username) {
        List<User> users = userRepository.findByUsernameContaining(username);
        for (User user : users) {
            rmProfilePic(user);
        }
        return new Response(200, OK, mapToPublicUsers(mapToTransferObjects(users)));
    }

    public Response searchTop100(String username) {
        List<User> users = userRepository.findTop100ByUsernameContaining(username);
        for (User user : users) {
            rmProfilePic(user);
        }

        return new Response(200, OK, mapToPublicUsers(mapToTransferObjects(users)));
    }

    public Response authUserByEmail(String email, String hash) {
        Optional<User> user = userRepository.findByEmail(email);
        Response response = new Response();

        if (hash == null || !user.isPresent()) {
            response.setMessage(UNAUTHORIZED);
            response.setContent(401);
            return response;
        }

        String token = accessTokenService.getTokenForUser(user.get().getUid());
        if (user.get().getPassword().equals(hash) && token != null) {
            response.setMessage(AUTHORIZED);
            response.setContent(token);
            return response;
        } else {
            response.setMessage(UNAUTHORIZED);
            response.setContent(401);
            response.setContent(null);
            return response;
        }
    }

    public Response loadProfilePic(String accessToken) {
        int uid = accessTokenService.getUserForToken(accessToken);
        if (uid == -1) {
            return null;
        }
        ProfilePicDTO profilePicDto = new ProfilePicDTO();
        Optional<User> user = userRepository.findById(uid);
        if (user.isPresent()) {
            profilePicDto.setProfilePic(Base64.convertToBase64(user.get().getProfilePic()));
        } else {
            profilePicDto.setProfilePic(null);
        }

        return new Response(200, OK, profilePicDto);
    }

    private List<PublicUserDTO> mapToPublicUsers(Collection<UserDTO> users) {
        if (users == null || users.isEmpty()) {
            return null;
        }

        List<PublicUserDTO> publicUsers = new ArrayList<>();
        for (UserDTO dto : users) {
            publicUsers.add(mapper.mapToPublicUser(dto));
        }
        return publicUsers;
    }

    private List<UserDTO> mapToTransferObjects(Collection<User> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        List<UserDTO> transferObjects = new ArrayList<>();
        for (User user : entities) {
            transferObjects.add(mapper.mapToTransferObject(user));
        }
        return transferObjects;
    }

    private void rmProfilePic(UserDTO dto) {
        dto.setProfilePic(null);
    }

    private void rmProfilePic(Optional<User> dto) {
        if (dto.isPresent()) {
            dto.get().setProfilePic(null);
        }
    }

    private void rmProfilePic(User user) {
        user.setProfilePic(null);
    }
}
