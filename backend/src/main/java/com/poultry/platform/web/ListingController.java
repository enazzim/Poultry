package com.poultry.platform.web;

import com.poultry.platform.dto.ListingDtos;
import com.poultry.platform.security.SecurityUtils;
import com.poultry.platform.security.UserPrincipal;
import com.poultry.platform.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping("/listings")
    public ListingDtos.ListingResponse create(@Valid @RequestBody ListingDtos.CreateListingRequest request) {
        return listingService.create(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/listings")
    public List<ListingDtos.ListingResponse> feed(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String side,
            @RequestParam(required = false, defaultValue = "ING") String listType) {
        return listingService.feed(categoryId, regionCode, side, listType);
    }

    @GetMapping("/listings/mine")
    public List<ListingDtos.ListingResponse> mine() {
        UserPrincipal user = SecurityUtils.currentUser();
        return listingService.myListings(user.getOrganizationId());
    }

    @GetMapping("/listings/{id}")
    public ListingDtos.ListingResponse detail(@PathVariable Long id) {
        return listingService.get(id);
    }

    @PostMapping("/listings/{id}/close")
    public ListingDtos.ListingResponse close(@PathVariable Long id) {
        return listingService.close(SecurityUtils.currentUser(), id);
    }

    @PostMapping("/listings/{id}/interests")
    public void interest(@PathVariable Long id) {
        listingService.addInterest(SecurityUtils.currentUser(), id);
    }

    @PostMapping("/listings/{id}/inquiries")
    public ListingDtos.InquiryResponse inquiry(@PathVariable Long id,
                                               @Valid @RequestBody ListingDtos.InquiryRequest request) {
        return listingService.addInquiry(SecurityUtils.currentUser(), id, request);
    }

    @GetMapping("/inquiries/received")
    public List<ListingDtos.InquiryResponse> received() {
        return listingService.receivedInquiries(SecurityUtils.currentUser().getOrganizationId());
    }

    @GetMapping("/inquiries/sent")
    public List<ListingDtos.InquiryResponse> sent() {
        return listingService.sentInquiries(SecurityUtils.currentUser().getOrganizationId());
    }

    @PutMapping("/preferences/me")
    public ListingDtos.PreferenceResponse upsertPreference(@Valid @RequestBody ListingDtos.PreferenceRequest request) {
        return listingService.upsertPreference(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/preferences/me")
    public List<ListingDtos.PreferenceResponse> myPreferences() {
        return listingService.myPreferences(SecurityUtils.currentUser().getOrganizationId());
    }
}
