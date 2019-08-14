package de.leonlatsch.oliviabackend.util;

import de.leonlatsch.oliviabackend.dto.MessageDTO;
import de.leonlatsch.oliviabackend.entity.Message;
import de.leonlatsch.oliviabackend.entity.User;
import de.leonlatsch.oliviabackend.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;

public class DatabaseMapper {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMapper.class);

    public UserDTO mapToTransferObject(User user, boolean withPassword) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setUid(user.getUid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        if (withPassword) {
            dto.setPassword(user.getPassword());
        }
        dto.setProfilePic(Base64.convertToBase64(user.getProfilePic()));
        dto.setProfilePicTn(Base64.convertToBase64(user.getProfilePicTn()));
        return dto;
    }

    public UserDTO mapToTransferObject(User user) {
        return mapToTransferObject(user, false);
    }

    public User mapToEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        try {
            Blob profilePic = null;
            Blob profilePicTn = null;

            if (userDTO.getProfilePic() != null) {
                profilePic = new SerialBlob(Base64.convertToBlob(userDTO.getProfilePic()));
                profilePicTn = new SerialBlob(ImageHelper.createThumbnail(profilePic));
            }
            return new User(userDTO.getUid(), userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword(), profilePic, profilePicTn);
        } catch (SQLException e) {
            log.error("" + e);
            return null;
        }
    }

    public MessageDTO mapToTransferObject(Message message) {
        if (message == null) {
            return null;
        }

        MessageDTO dto = new MessageDTO();
        dto.setMid(message.getMid());
        dto.setFrom(message.getFrom());
        dto.setTo(message.getTo());
        dto.setContent(Base64.convertToBase64(message.getContent()));
        dto.setType(message.getType());
        dto.setTimestamp(message.getTimestamp().toString()); // May need to reformat this
        return dto;
    }
}
