package com.woc.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.woc.dto.PINUpdateRequestObject;
import com.woc.dto.RiderDocuments;
import com.woc.dto.RiderLocaionUpdateRequest;
import com.woc.dto.RiderSearchCriteria;
import com.woc.entity.Rider;
import com.woc.entity.User;

@Repository
public class RiderRepositoryImpl implements RiderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Rider> findAll() {
        List<Rider> riders = entityManager.createNamedQuery("Rider.findAll", Rider.class).getResultList();
        return riders;
    }

    @Override
    public Rider findByID(long id) {
        List<Rider> riders = entityManager.createNamedQuery("Rider.findById").setParameter(1, id).getResultList();
        if (!riders.isEmpty()) {
            return riders.get(0);
        }
        return null;

    }

    @Transactional
    @Override
    public long addRider(Rider r) {
        // TODO Auto-generated method stub
        entityManager.persist(r);
        return r.getId();
    }

    @Override
    public com.woc.dto.Rider getRider(RiderSearchCriteria searchRider) {
        // TODO Auto-generated method stub
        if (searchRider.getPhoneNumber() != null) {
            String number = searchRider.getPhoneNumber();
            List<User> users = entityManager
                    .createNativeQuery("select * from User u where u.phone = " + number, User.class).getResultList();
            List<com.woc.dto.Rider> allRiders = new ArrayList();
            // for (User u : users) {
            System.out.println("user length : " + users.size());

            if (users.size() == 0) {
                return null;
            }
            User u = users.get(0);
            long userId = u.getId();
            List<Rider> riders = entityManager
                    .createNativeQuery("select * from Rider r where r.user_id = " + userId, Rider.class)
                    .getResultList();
            // for (Rider each : riders) {
            System.out.println("riders.size() : " + riders.size());
            if (riders.size() == 0) {
                return null;
            }

            Rider each = riders.get(0);
            com.woc.dto.Rider r = new com.woc.dto.Rider();
            r.setEmail(u.getEmail());
            r.setName(u.getName());
            r.setPhoneNumber(u.getPhone());
            r.setRiderID(each.getId());
            r.setUserId(userId);
            r.setDisabled(each.isIs_challenged());
            r.setDeviceID(each.getDeviceID());
            // Map<String, String> docs = new HashMap<String, String>();
            // docs.put("document_proof", each.getProof_of_challenge());
            RiderDocuments docs = new RiderDocuments();
            docs.setDisabilityProof(each.getProof_of_challenge());
            r.setPIN(each.getPin());
            // allRiders.add(r);
            r.setDocuments(docs);
            r.setLocation(each.getLocation());

            // }

            // }
            return r;

        } else {
            long riderId = searchRider.getRiderID();
            List<Rider> riders = entityManager
                    .createNativeQuery("select * from Rider r where r.id = " + riderId, Rider.class).getResultList();
            if (riders.size() == 0) {
                return null;
            }
            Rider rider = riders.get(0);
            long userid = rider.getUser_id();

            List<User> users = entityManager
                    .createNativeQuery("select * from User u where u.id = " + userid, User.class).getResultList();
            if (users.size() == 0) {
                return null;
            }
            User u = users.get(0);
            // for (Rider each : riders) {
            com.woc.dto.Rider r = new com.woc.dto.Rider();
            r.setEmail(u.getEmail());
            r.setName(u.getName());
            // r.setPIN(u.get);
            r.setPhoneNumber(u.getPhone());
            r.setRiderID(rider.getId());
            r.setDisabled(rider.isIs_challenged());
            // Map<String, String> docs = new HashMap<String, String>();
            // docs.put("document_proof", rider.getProof_of_challenge());
            RiderDocuments doc = new RiderDocuments();
            doc.setDisabilityProof(rider.getProof_of_challenge());
            r.setUserId(u.getId());
            r.setDeviceID(rider.getDeviceID());
            r.setDocuments(doc);
            r.setPIN(rider.getPin());
            r.setLocation(rider.getLocation());
            return r;
            // } // return riders;
        }
    }

    @Transactional
    @Override
    public long updateRiderLocation(RiderLocaionUpdateRequest request) {
        long updated = 0;
        Query q = entityManager
                .createNativeQuery("Update Rider r set r.location = :location where r.id = " + request.getRiderId());
        q.setParameter("location", request.getLocation());
        updated = q.executeUpdate();
        return updated;
    }

    @Transactional
    @Override
    public com.woc.dto.Rider updateRiderPin(PINUpdateRequestObject updateReq) {
        // TODO Auto-generated method stub
        long riderId = updateReq.getRiderID();
        String pin = updateReq.getPIN();
       
        Query q = entityManager.createNativeQuery("update Rider r set r.pin = " + pin + " where r.id =" + riderId,
                Rider.class);
        long rowsUpdated = q.executeUpdate();
         if (rowsUpdated != 0 ){
             RiderSearchCriteria search = new RiderSearchCriteria();
             search.setRiderID(updateReq.getRiderID());
             System.out.println("updated row : " + rowsUpdated);
             return getRider(search);
         }
        return new com.woc.dto.Rider();
    }

    @Transactional
    @Override
    public long updateRiderData(com.woc.dto.Rider r) {

        if (r.getRiderID() == 0 && (r.getPhoneNumber() == null || r.getPhoneNumber().trim().isEmpty())) {
            return 0l;
        } else {
            long rowsUpdated = 0;
            long userId = 0;
            if (r.getRiderID() != 0) {
                RiderSearchCriteria search = new RiderSearchCriteria();
                search.setRiderID(r.getRiderID());
                com.woc.dto.Rider fetched_Rider = getRider(search);

                userId = fetched_Rider.getUserId();

            } else {
                List<User> users = entityManager
                        .createNativeQuery("select * from User u where u.phone = " + r.getPhoneNumber(), User.class)
                        .getResultList();
                if (users.size() == 0) {
                    return 0l;
                }
                User u = users.get(0);
                userId = u.getId();
            }

            if (r.getDocuments() != null && (r.getDeviceID() != null && !r.getDeviceID().trim().isEmpty())) {
                Query q = entityManager.createNativeQuery(
                        "update Rider r set r.proof_of_challenge = :proof_of_challenge, r.deviceID = :deviceid where r.user_id ="
                                + userId,
                        Rider.class);
                q.setParameter("proof_of_challenge", r.getDocuments().getDisabilityProof());
                q.setParameter("deviceid", r.getDeviceID());
                rowsUpdated = q.executeUpdate();
                System.out.println("updated row : " + rowsUpdated);
                return rowsUpdated;
            } else if (r.getDocuments() != null) {
                Query q = entityManager.createNativeQuery(
                        "update Rider r set r.proof_of_challenge = :proof_of_challenge where r.user_id =" + userId,
                        Rider.class);
                q.setParameter("proof_of_challenge", r.getDocuments().getDisabilityProof());
                rowsUpdated = q.executeUpdate();
                System.out.println("updated row : " + rowsUpdated);
                return rowsUpdated;
            } else if (r.getDeviceID() != null && !r.getDeviceID().trim().isEmpty()) {
                Query q = entityManager.createNativeQuery(
                        "update Rider r set r.deviceID = :deviceid where r.user_id =" + userId, Rider.class);
                q.setParameter("deviceid", r.getDeviceID());
                rowsUpdated = q.executeUpdate();
                System.out.println("updated row : " + rowsUpdated);
                return rowsUpdated;
            }
            return 0l;
        }

    }

}
